# Picadito

REST API for booking padel and 5-a-side football courts. Players search for clubs, check
available time slots and book a court with a deposit payment; club owners manage their
courts, opening hours and pricing.

> 🚧 **Status:** in active development. Built in 2-week Scrum sprints (Nov 2026 – Jan 2027).

## Tech stack

- Java 21
- Spring Boot 4
- Maven
- PostgreSQL
- Flyway
- Docker

More tools will be added as the project evolves (Spring Security + JWT, GitHub Actions, Testcontainers).

## Architecture

The code is organized **by feature** instead of by technical layer, so everything related
to a business capability lives together:

| Package        | Responsibility                                              |
|----------------|-------------------------------------------------------------|
| `shared`       | Cross-cutting concerns: configuration, error handling       |
| `auth`         | Registration, login and JWT-based security                  |
| `user`         | User accounts and roles (player, club owner, admin)         |
| `club`         | Clubs, courts, opening hours and blocked slots              |
| `booking`      | Availability, reservations and their lifecycle              |
| `payment`      | Deposit payments via Mercado Pago                           |
| `pricing`      | Rule-based court pricing                                    |
| `notification` | Domain events and email notifications                       |

## Running locally

Requirements: JDK 21 and Docker.

1. Copy the environment template and set a password:
   ```bash
   cp .env.example .env
   ```
2. Start PostgreSQL:
   ```bash
   docker compose up -d
   ```
3. Run the application (the `dev` profile is active by default):
   ```bash
   ./mvnw spring-boot:run
   ```

Database migrations are applied automatically by Flyway on startup.
The API starts on `http://localhost:8080`.

## Author

Tomás Méndez Nacher — [GitHub](https://github.com/TomiiMN)
