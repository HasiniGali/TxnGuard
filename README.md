# TxnGuard – Transaction-Safe Event Booking System

TxnGuard is a database-driven event booking system built using Java, Spring Boot, and PostgreSQL. It helps users view events, book seats, cancel bookings, and view their booking history.

## Features

- View available events
- Book seats for events
- Cancel confirmed bookings
- View booking history
- Prevent duplicate confirmed bookings
- Check event capacity before confirming bookings
- Use database transactions for booking operations
- Store event and booking information in PostgreSQL
- Responsive web dashboard
- Query performance analysis

## Technology Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- JDBC
- HTML
- CSS
- JavaScript

## Database Structure

The project uses three main tables:

| Table | Purpose |
|---|---|
| `events` | Stores event information and capacity |
| `users` | Stores user information |
| `bookings` | Stores booking details and status |

## How to Run the Project

### Prerequisites

- Java 21
- PostgreSQL
- Git
- Maven, or the included Maven Wrapper

### 1. Clone the repository

```bash
git clone https://github.com/HasiniGali/TxnGuard.git
cd TxnGuard
```

### 2. Create the database

Create a PostgreSQL database named `txnguard`.

The database tables and required initial data must also be created before running the application. Add a database setup SQL script to this repository if one is available.

### 3. Configure the database

Configure the PostgreSQL connection in:

`src/main/resources/application.properties`

Use environment variables for credentials. Do not store your actual database password in the source code.

For example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/txnguard
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
```

### 4. Run the application on Windows

Open PowerShell in the project directory and enter your PostgreSQL password when prompted:

```powershell
$env:DB_PASSWORD = Read-Host "Enter your PostgreSQL password"
```

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

### 5. Open the dashboard

Visit:

http://localhost:8080/

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/events` | View all events |
| GET | `/api/events/{eventId}` | View a specific event |
| POST | `/api/bookings` | Create a booking |
| GET | `/api/bookings` | View all bookings |
| GET | `/api/bookings/user/{userId}` | View bookings for a user |
| DELETE | `/api/bookings/{bookingId}` | Cancel a booking |

### Example booking request

```json
{
  "userId": 1,
  "eventId": 1
}
```

## Database Concepts Demonstrated

- ACID transactions
- Transaction management
- Concurrency control
- Duplicate booking prevention
- Capacity validation
- Database constraints and indexes
- Query performance analysis

## Project Status

The event dashboard, booking APIs, PostgreSQL integration, booking creation and cancellation, booking history, and duplicate booking prevention have been implemented.

Performance and reliability results should be documented after the corresponding tests have been completed and verified.

## Author

**Hasini Gali**

GitHub: [HasiniGali](https://github.com/HasiniGali)
