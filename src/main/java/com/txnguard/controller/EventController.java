package com.txnguard.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final JdbcTemplate jdbcTemplate;

    public EventController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Get all events with available seats
    @GetMapping
    public List<Map<String, Object>> getAllEvents() {

        return jdbcTemplate.queryForList(
            """
            SELECT
                event_id,
                title,
                capacity,
                booked_count,
                capacity - booked_count AS available_seats
            FROM events
            ORDER BY event_id
            """
        );
    }

    // Get one event by ID
    @GetMapping("/{eventId}")
    public Map<String, Object> getEventById(
            @PathVariable long eventId) {

        List<Map<String, Object>> events =
            jdbcTemplate.queryForList(
                """
                SELECT
                    event_id,
                    title,
                    capacity,
                    booked_count,
                    capacity - booked_count AS available_seats
                FROM events
                WHERE event_id = ?
                """,
                eventId
            );

        if (events.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Event not found"
            );
        }

        return events.get(0);
    }
}