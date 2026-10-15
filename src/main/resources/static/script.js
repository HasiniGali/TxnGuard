"use strict";

/* =========================
   API CONFIGURATION
========================= */

const EVENTS_API = "/api/events";
const BOOKINGS_API = "/api/bookings";


/* =========================
   HELPER: SAFE NUMBER
========================= */

function toNumber(value, fallback = 0) {
    const number = Number(value);

    return Number.isFinite(number) ? number : fallback;
}


/* =========================
   HELPER: SHOW MESSAGE
========================= */

function showMessage(message, isError = false) {
    const messageElement = document.getElementById("message");

    messageElement.textContent = message;
    messageElement.classList.toggle("error", isError);
}


/* =========================
   HELPER: API ERROR MESSAGE
========================= */

async function getErrorMessage(response) {
    try {
        const data = await response.json();

        return (
            data.detail ||
            data.message ||
            data.error ||
            `Request failed with HTTP ${response.status}`
        );
    } catch {
        return `Request failed with HTTP ${response.status}`;
    }
}


/* =========================
   LOAD EVENTS
========================= */

async function loadEvents() {
    const container = document.getElementById("events");

    container.replaceChildren();

    try {
        const response = await fetch(EVENTS_API);

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const events = await response.json();

        if (!Array.isArray(events)) {
            throw new Error("The events API did not return an array.");
        }

        document.getElementById("totalEvents").textContent =
            events.length;

        const availableEvents = events.filter(event => {
            const capacity = toNumber(event.capacity);
            const booked = toNumber(event.booked_count);

            return capacity > booked;
        });

        document.getElementById("availableEvents").textContent =
            availableEvents.length;

        if (events.length === 0) {
            const empty = document.createElement("p");

            empty.className = "empty-row";
            empty.textContent = "No events are currently available.";

            container.appendChild(empty);

            return;
        }

        events.forEach(event => {
            container.appendChild(createEventCard(event));
        });

    } catch (error) {
        console.error("Error loading events:", error);

        showMessage(
            `Unable to load events: ${error.message}`,
            true
        );

        const errorText = document.createElement("p");

        errorText.className = "empty-row";
        errorText.textContent =
            "Check that Spring Boot is running and the events API is available.";

        container.appendChild(errorText);
    }
}


/* =========================
   CREATE EVENT CARD
========================= */

function createEventCard(event) {
    const card = document.createElement("article");

    card.className = "event-card";

    const title = document.createElement("h3");

    title.textContent = event.title || "Untitled Event";

    const capacity = toNumber(event.capacity);
    const booked = toNumber(event.booked_count);

    const available = Math.max(0, capacity - booked);

    const capacityInfo = document.createElement("p");

    capacityInfo.textContent = `Total capacity: ${capacity}`;

    const bookedInfo = document.createElement("p");

    bookedInfo.textContent = `Confirmed bookings: ${booked}`;

    const seatsInfo = document.createElement("p");

    seatsInfo.className = "event-seats";

    seatsInfo.textContent = `Available seats: ${available}`;

    const button = document.createElement("button");

    button.textContent =
        available > 0 ? "Book Event" : "Fully Booked";

    button.disabled = available === 0;

    button.addEventListener("click", () => {
        bookEvent(event.event_id, event.title);
    });

    card.append(
        title,
        capacityInfo,
        bookedInfo,
        seatsInfo,
        button
    );

    return card;
}


/* =========================
   CREATE BOOKING
========================= */

async function bookEvent(eventId, title) {
    const userInput = prompt(
        `Enter your user ID to book "${title}".`
    );

    if (userInput === null) {
        return;
    }

    const userId = Number(userInput);

    if (
        !Number.isSafeInteger(userId) ||
        userId <= 0
    ) {
        alert("Enter a valid positive user ID.");
        return;
    }

    try {
        const response = await fetch(BOOKINGS_API, {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                userId: userId,
                eventId: eventId
            })
        });

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const result = await response.json();

        alert(
            `Booking successful!\nBooking ID: ${result.booking_id}`
        );

        showMessage("Booking created successfully.");

        await refreshDashboard();

    } catch (error) {
        console.error("Booking error:", error);

        alert(error.message);

        showMessage(
            `Booking failed: ${error.message}`,
            true
        );
    }
}


/* =========================
   LOAD BOOKING HISTORY
========================= */

async function loadBookings() {
    const table = document.getElementById("bookingTable");

    table.replaceChildren();

    try {
        const response = await fetch(BOOKINGS_API);

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const bookings = await response.json();

        if (!Array.isArray(bookings)) {
            throw new Error("The bookings API did not return an array.");
        }

        document.getElementById("totalBookings").textContent =
            bookings.length;

        const confirmedCount = bookings.filter(
            booking => booking.status === "CONFIRMED"
        ).length;

        document.getElementById("confirmedBookings").textContent =
            confirmedCount;

        if (bookings.length === 0) {
            const row = table.insertRow();

            const cell = row.insertCell();

            cell.colSpan = 5;
            cell.className = "empty-row";
            cell.textContent = "No bookings found.";

            return;
        }

        bookings.forEach(booking => {
            table.appendChild(createBookingRow(booking));
        });

    } catch (error) {
        console.error("Error loading bookings:", error);

        const row = table.insertRow();

        const cell = row.insertCell();

        cell.colSpan = 5;
        cell.className = "empty-row";
        cell.textContent = `Unable to load bookings: ${error.message}`;

        showMessage(
            `Unable to load booking history: ${error.message}`,
            true
        );
    }
}


/* =========================
   CREATE BOOKING TABLE ROW
========================= */

function createBookingRow(booking) {
    const row = document.createElement("tr");

    // Booking ID
    const idCell = row.insertCell();

    idCell.textContent = booking.booking_id;

    // User name
    const userCell = row.insertCell();

    userCell.textContent =
        booking.user_name || `User ${booking.user_id}`;

    // Event title
    const eventCell = row.insertCell();

    eventCell.textContent =
        booking.event_title || `Event ${booking.event_id}`;

    // Booking status
    const statusCell = row.insertCell();

    const statusBadge = document.createElement("span");

    if (booking.status === "CONFIRMED") {
        statusBadge.className = "status-confirmed";
    } else {
        statusBadge.className = "status-cancelled";
    }

    statusBadge.textContent = booking.status;

    statusCell.appendChild(statusBadge);

    // Action
    const actionCell = row.insertCell();

    if (booking.status === "CONFIRMED") {
        const cancelButton = document.createElement("button");

        cancelButton.className = "cancel-btn";
        cancelButton.textContent = "Cancel";

        cancelButton.addEventListener("click", () => {
            cancelBooking(booking.booking_id, cancelButton);
        });

        actionCell.appendChild(cancelButton);

    } else {
        const noAction = document.createElement("span");

        noAction.className = "no-action";
        noAction.textContent = "—";

        actionCell.appendChild(noAction);
    }

    return row;
}


/* =========================
   CANCEL BOOKING
========================= */

async function cancelBooking(bookingId, button) {
    const confirmed = confirm(
        `Are you sure you want to cancel booking ${bookingId}?`
    );

    if (!confirmed) {
        return;
    }

    button.disabled = true;
    button.textContent = "Cancelling...";

    try {
        const response = await fetch(
            `${BOOKINGS_API}/${encodeURIComponent(bookingId)}`,
            {
                method: "DELETE"
            }
        );

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const result = await response.json();

        alert(
            result.message || "Booking cancelled successfully."
        );

        showMessage("Booking cancelled successfully.");

        await refreshDashboard();

    } catch (error) {
        console.error("Cancellation error:", error);

        alert(error.message);

        showMessage(
            `Cancellation failed: ${error.message}`,
            true
        );

        button.disabled = false;
        button.textContent = "Cancel";
    }
}


/* =========================
   REFRESH DASHBOARD
========================= */

async function refreshDashboard() {
    await Promise.all([
        loadEvents(),
        loadBookings()
    ]);
}


/* =========================
   INITIALIZE DASHBOARD
========================= */

document.addEventListener("DOMContentLoaded", () => {
    refreshDashboard();
});