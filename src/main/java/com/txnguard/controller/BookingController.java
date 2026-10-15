package com.txnguard.controller;

import com.txnguard.service.BookingService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final JdbcTemplate jdbcTemplate;

    public BookingController(
            BookingService bookingService,
            JdbcTemplate jdbcTemplate) {

        this.bookingService = bookingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // CREATE BOOKING
    @PostMapping
    public Map<String, Object> createBooking(
            @RequestBody BookingRequest request) {

        return bookingService.createBooking(
                request.userId(),
                request.eventId()
        );
    }

    // CANCEL BOOKING
    @DeleteMapping("/{bookingId}")
    public Map<String, Object> cancelBooking(
            @PathVariable long bookingId) {

        return bookingService.cancelBooking(bookingId);
    }

    // GET ALL BOOKINGS WITH EVENT AND USER DETAILS
    @GetMapping
    public List<Map<String, Object>> getAllBookings() {

        return jdbcTemplate.queryForList(
                """
                SELECT
                    b.booking_id,
                    b.user_id,
                    u.name AS user_name,
                    b.event_id,
                    e.title AS event_title,
                    b.status,
                    b.created_at
                FROM bookings b
                JOIN users u ON b.user_id = u.user_id
                JOIN events e ON b.event_id = e.event_id
                ORDER BY b.booking_id DESC
                """
        );
    }

    // GET BOOKINGS FOR A PARTICULAR USER
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getBookingsByUser(
            @PathVariable long userId) {

        return jdbcTemplate.queryForList(
                """
                SELECT
                    b.booking_id,
                    b.user_id,
                    u.name AS user_name,
                    b.event_id,
                    e.title AS event_title,
                    b.status,
                    b.created_at
                FROM bookings b
                JOIN users u ON b.user_id = u.user_id
                JOIN events e ON b.event_id = e.event_id
                WHERE b.user_id = ?
                ORDER BY b.booking_id DESC
                """,
                userId
        );
    }

    public record BookingRequest(long userId, long eventId) {
    }
}