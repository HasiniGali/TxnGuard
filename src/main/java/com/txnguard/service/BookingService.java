package com.txnguard.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class BookingService {

    private final JdbcTemplate jdbcTemplate;

    public BookingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // CREATE BOOKING
    @Transactional
    public Map<String, Object> createBooking(long userId, long eventId) {

        // 1. Check whether the user exists.
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = ?",
                Integer.class,
                userId
        );

        if (userCount == null || userCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        // 2. Lock the event row to prevent overbooking.
        var events = jdbcTemplate.queryForList(
                """
                SELECT event_id, title, capacity, booked_count
                FROM events
                WHERE event_id = ?
                FOR UPDATE
                """,
                eventId
        );

        if (events.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
            );
        }

        Map<String, Object> event = events.get(0);

        // 3. Prevent duplicate confirmed bookings.
        Integer existingBooking = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM bookings
                WHERE user_id = ?
                  AND event_id = ?
                  AND status = 'CONFIRMED'
                """,
                Integer.class,
                userId,
                eventId
        );

        if (existingBooking != null && existingBooking > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already booked this event"
            );
        }

        // 4. Check event capacity.
        int capacity =
                ((Number) event.get("capacity")).intValue();

        int bookedCount =
                ((Number) event.get("booked_count")).intValue();

        if (bookedCount >= capacity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Event is fully booked"
            );
        }

        // 5. Insert the booking.
        Long bookingId = jdbcTemplate.queryForObject(
                """
                INSERT INTO bookings (user_id, event_id, status)
                VALUES (?, ?, 'CONFIRMED')
                RETURNING booking_id
                """,
                Long.class,
                userId,
                eventId
        );

        // 6. Increase the booked count.
        jdbcTemplate.update(
                """
                UPDATE events
                SET booked_count = booked_count + 1
                WHERE event_id = ?
                """,
                eventId
        );

        // 7. Return booking confirmation.
        return Map.of(
                "booking_id", bookingId,
                "user_id", userId,
                "event_id", eventId,
                "event_title", event.get("title"),
                "status", "CONFIRMED",
                "message", "Booking successful"
        );
    }

    // CANCEL BOOKING
    @Transactional
    public Map<String, Object> cancelBooking(long bookingId) {

        // 1. Find and lock the booking.
        var bookings = jdbcTemplate.queryForList(
                """
                SELECT booking_id, event_id, status
                FROM bookings
                WHERE booking_id = ?
                FOR UPDATE
                """,
                bookingId
        );

        if (bookings.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }

        Map<String, Object> booking = bookings.get(0);

        String status = booking.get("status").toString();

        long eventId =
                ((Number) booking.get("event_id")).longValue();

        // 2. Prevent repeated cancellation.
        if (!status.equals("CONFIRMED")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Booking is already cancelled or is not confirmed"
            );
        }

        // 3. Lock the event row.
        var events = jdbcTemplate.queryForList(
                """
                SELECT event_id
                FROM events
                WHERE event_id = ?
                FOR UPDATE
                """,
                eventId
        );

        if (events.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
            );
        }

        // 4. Mark the booking as cancelled.
        jdbcTemplate.update(
                """
                UPDATE bookings
                SET status = 'CANCELLED'
                WHERE booking_id = ?
                  AND status = 'CONFIRMED'
                """,
                bookingId
        );

        // 5. Decrease the event's booked count.
        int updatedRows = jdbcTemplate.update(
                """
                UPDATE events
                SET booked_count = booked_count - 1
                WHERE event_id = ?
                  AND booked_count > 0
                """,
                eventId
        );

        // Throwing an exception rolls back the transaction.
        if (updatedRows != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Could not update event booking count"
            );
        }

        // 6. Return cancellation confirmation.
        return Map.of(
                "booking_id", bookingId,
                "event_id", eventId,
                "status", "CANCELLED",
                "message", "Booking cancelled successfully"
        );
    }
}