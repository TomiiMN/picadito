# Picadito

REST API for booking padel and 5-a-side football courts. Players search for clubs, check
available time slots and book a court with a deposit payment; club owners manage their
courts, opening hours and pricing.

> 🚧 **Status:** in active development. Built in 2-week Scrum sprints (Nov 2026 – Jan 2027).

## Tech stack

- Java 21
- Spring Boot 4
- Maven

More tools will be added as the project evolves (PostgreSQL, Flyway, Spring Security + JWT,
Docker, GitHub Actions, Testcontainers).

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

Requirements: JDK 21.

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## Author

Tomás Méndez Nacher — [GitHub](https://github.com/TomiiMN)