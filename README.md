# GuestFind

## Project Description
GuestFind is a hotel-focused Lost & Found and Guest Engagement platform. It streamlines how hotels record lost items, register found items, match them through staff workflows, allow guests to submit claims, and coordinate item delivery once ownership is validated. The system enforces role-based permissions (ADMIN, STAFF, GUEST) and provides notifications and statistics to reduce manual tracking and improve guest satisfaction. A lightweight static frontend scaffold is included; you can later replace it with a modern SPA (React/Vue/etc.).

> Summary: Centralized lost & found lifecycle management (report → match → claim → approve → deliver) with secure JWT authentication and role-based access.

## Screenshots

| Feature | Screenshot |
|---------|------------|
| Dashboard | ![Dashboard](https://i.ibb.co/sdZdyW5L/Dashboard.png) |
| Lost Item Submission | ![Lost Item Form](https://i.ibb.co/Qv6b6NpJ/Lost-Item-Submission.png) |
| Found Items List | ![Found Items](https://i.ibb.co/pmpbK4w/Found-Items-List.png) |
| Claims Management | ![Claims](https://i.ibb.co/5ghtPccw/Claims-Management.png) |
| Notifications Center | ![Notifications](https://i.ibb.co/4nK83BVq/Notifications-Center.png) |

Add more later: Staff Profile, Delivery Tracking, Stats Overview.

## Table of Contents
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Build & Run](#build--run)
- [API Overview](#api-overview)
- [Authentication & Security](#authentication--security)
- [Error & Response Format](#error--response-format)
- [Common Workflows](#common-workflows)
- [Development Tips](#development-tips)
- [Future Improvements](#future-improvements)
- [License](#license)

## Architecture
Layered Spring Boot application:
- Controller layer: REST endpoints with method-level security (`@PreAuthorize`).
- Service layer: Business logic & orchestration.
- Repository layer: Spring Data JPA for persistence.
- Security: Stateless JWT auth filter + custom handlers.
- DTO mapping via ModelMapper + Lombok for boilerplate reduction.

## Features
- Guest registration & authentication (JWT)
- Admin & Staff management
- Report Lost Items (guests) and Found Items (guests/staff/admin)
- Archive / Unarchive workflows (admin/staff)
- Claim workflow (guests submit, staff/admin approve/reject)
- Delivery management for approved claims
- Role-based access control (ADMIN, STAFF, GUEST)
- Per-role statistics endpoints
- Notification system (guest-specific and admin-wide)
- CORS configurable origins
- Email (SMTP) integration (Spring Mail)

## Tech Stack
- Java 17
- Spring Boot 3.5.5
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Mail
- JWT (io.jsonwebtoken 0.11.5)
- MySQL 8+
- ModelMapper 3.0.0
- Lombok
- Maven build

## Project Structure
```
backend/
  src/main/java/com/gdse/aad/backend/
    config/        # Security & CORS config
    controller/    # REST controllers
    dto/           # Data Transfer Objects
    entity/        # JPA entities
    exception/     # Custom handlers, exceptions
    repository/    # Spring Data JPA repositories
    service/       # Interfaces & implementations
    util/          # JWT utilities, helpers
  src/main/resources/
    application.properties
frontend/
  index.html
  style/
  style-js/
```

## Getting Started
End-to-end instructions for local development (backend + static frontend prototype).

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8+

### Backend Setup
1. Clone repository:
  ```bash
  git clone <https://github.com/GamithaGimhana/GuestFind>
  cd GuestFind/backend
  ```
2. Configure `application.properties` (or create `application-local.properties`). Replace:
  - `spring.datasource.username`
  - `spring.datasource.password`
  - `jwt.secretKey` (use a long random string; DO NOT commit secrets)

3. Create database if not auto-created:
  ```sql
  CREATE DATABASE guestfind;
  ```
4. Build & run:
  ```bash
  mvn clean install
  mvn spring-boot:run
  ```
6. Backend available at `http://localhost:8080`.

### Initial Admin User
No automatic seeding is shown; options:
1. Temporarily enable an admin registration path.
2. Insert a user row directly in the database with role ADMIN and a BCrypt-hashed password.
3. Create a one-off CommandLineRunner (remove afterward).

If automatic seeding is shown in the logs, you can skip this step.

```
  Default username : admin@guestfind.com
  Default password : admin123
  ```

### Frontend Prototype
Located in `frontend/`. Ways to load:
1. Open `index.html` directly (may hit CORS restrictions for API calls).

### Environment Variable Pattern
You can externalize secrets:
```
spring.datasource.password=${DB_PASS}
jwt.secretKey=${JWT_SECRET}
spring.mail.password=${MAIL_PASS}
```
Export variables before starting the app.

### Tests
```bash
mvn test
```

### Optional Developer Enhancements
- Add `spring-boot-devtools` for hot reload.
- Introduce Flyway or Liquibase for schema migrations.
- Add Swagger/OpenAPI (springdoc-openapi) for live docs.

## Configuration
All core configurations are in `backend/src/main/resources/application.properties`:

| Property | Description |
|----------|-------------|
| `spring.datasource.url` | JDBC URL to MySQL database |
| `spring.datasource.username` | DB username |
| `spring.datasource.password` | DB password (change in prod) |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy (`update` for dev) |
| `cors.allowed-origins` | Comma-separated allowed origins for CORS |
| `jwt.secretKey` | Secret for signing JWT (replace!) |
| `jwt.expiration` | JWT validity in ms |
| `spring.mail.*` | Outgoing SMTP settings |

Security-sensitive values (DB password, JWT secret, mail credentials) should be externalized via environment variables or a secrets manager in production.

## Build & Run
From the `backend` directory:
```
# Run tests & build
mvn clean install

# Run backend
mvn spring-boot:run
```
The application starts (default) on: `http://localhost:8080`

### Packaging
```
mvn -DskipTests package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## API Overview
(Representative endpoints; see code for full details.)

### Auth (`/auth`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | /auth/register | ADMIN | Register staff/admin accounts |
| POST | /auth/login | Public | Login (staff/admin) -> JWT |
| POST | /auth/guest/register | Public | Guest self-registration |
| POST | /auth/guest/login | Public | Guest login |

### Guests (`/guests`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | /guests/profile | GUEST (via token) | Get own profile |
| PUT | /guests/profile | GUEST | Update profile |
| PUT | /guests/me/password | GUEST | Change password |
| GET | /guests | ADMIN | List all guests |
| GET | /guests/{id} | ADMIN | Get guest by id |
| PUT | /guests/{id} | ADMIN | Update guest |
| DELETE | /guests/{id} | ADMIN | Delete guest |

### Staff (`/staff`)
| GET /staff/profile, PUT /staff/profile, PUT /staff/me/password (self) |
| Admin management: /staff/all, /staff/admin/{email}, PUT /staff/admin/{email}, CRUD by /staff/{id} |

### Hotels (`/hotels`)
Admin CRUD endpoints for hotel entities.

### Lost Items (`/lost-items`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | /lost-items | GUEST | Create lost item |
| GET | /lost-items/me | GUEST | List own lost items |
| GET | /lost-items | ADMIN, STAFF | List all |
| GET | /lost-items/{id} | ADMIN, STAFF, GUEST | Get by id |
| PUT | /lost-items/{id} | GUEST | Update own item |
| DELETE | /lost-items/{id} | GUEST | Delete own item |
| PUT | /lost-items/{id}/status | STAFF | Update status |
| PUT | /lost-items/{id}/archive | ADMIN | Archive |
| GET | /lost-items/archived | ADMIN, STAFF | List archived |
| PUT | /lost-items/{id}/unarchive | ADMIN | Unarchive |

### Found Items (`/found-items`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | /found-items | ADMIN, STAFF, GUEST | Report found item |
| GET | /found-items | ADMIN, STAFF, GUEST | List all |
| GET | /found-items/unclaimed | ADMIN, STAFF, GUEST | List unclaimed |
| GET | /found-items/{id} | ADMIN, STAFF, GUEST | Get by id |
| PUT | /found-items/{id} | ADMIN, STAFF | Update |
| PUT | /found-items/{id}/archive | ADMIN | Archive |
| GET | /found-items/archived | ADMIN, STAFF | List archived |
| PUT | /found-items/{id}/unarchive | ADMIN | Unarchive |
| POST | /found-items/matches?foundItemId=&lostItemId= | ADMIN, STAFF | Match lost/found |

### Claims (`/claims`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | /claims | GUEST | Create claim for found item |
| GET | /claims | ADMIN, STAFF | List all claims |
| GET | /claims/{id} | ADMIN, STAFF | Get claim |
| PUT | /claims/{id}/reply | ADMIN, STAFF | Reply to claim |
| PUT | /claims/{id}/approve | ADMIN, STAFF | Approve claim |
| PUT | /claims/{id}/reject | ADMIN, STAFF | Reject claim |

### Deliveries (`/deliveries`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | /deliveries | ADMIN, GUEST | Create delivery request |
| GET | /deliveries | ADMIN | List all |
| GET | /deliveries/lost/{lostItemId} | ADMIN, GUEST | Get by lost item |
| PUT | /deliveries/{id}/status | ADMIN | Update status |

### Notifications (`/notifications`)
| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | /notifications/me | GUEST | My notifications |
| PUT | /notifications/{id}/read | GUEST | Mark as read |
| GET | /notifications | ADMIN | All notifications |

### Stats (`/stats`)
| GET /stats/admin (ADMIN), /stats/staff (STAFF), /stats/guest (GUEST) for role-specific dashboards.

## Authentication & Security
- Stateless JWT via custom filter (`JwtAuthFilter`) inserted before `UsernamePasswordAuthenticationFilter`.
- `/auth/**` endpoints are public; all others require authentication.
- Method-level restrictions with `@PreAuthorize` enforce roles.
- Passwords hashed via BCrypt.
- CORS origins controlled by `cors.allowed-origins` property.
- Custom handlers: `CustomAuthenticationEntryPoint` and `CustomAccessDeniedHandler` provide consistent error responses.

### Obtaining a Token
1. Register (if guest or via admin for staff).
2. Login (`/auth/login` or `/auth/guest/login`).
3. Use `Authorization: Bearer <token>` header for protected endpoints.

### Token Expiration
Controlled by `jwt.expiration` (milliseconds). Refresh strategy not yet implemented.

## Error & Response Format
All responses are wrapped in `ApiResponseDTO`:
```
{
  "status": 200,
  "message": "OK",
  "data": { ... }
}
```
Errors (401/403/404/etc.) use similar shape with appropriate HTTP status codes.

## Common Workflows
### Guest reports lost item
1. Guest login → token
2. POST /lost-items with details
3. Staff views & updates status
4. If found item matched, guest can submit claim
5. Staff/Admin approves claim → delivery scheduled

### Staff matches found & lost items
1. List unclaimed `/found-items/unclaimed`
2. List lost `/lost-items` (admin/staff)
3. POST `/found-items/matches?foundItemId=..&lostItemId=..`

### Claim approval
1. Guest creates claim
2. Staff/Admin reviews `/claims`
3. Approve or reject
4. Delivery optionally created for logistics

## Development Tips
- Enable SQL logging by uncommenting logging properties in `application.properties`.
- Use Postman/Insomnia with an environment variable for `{{baseUrl}}` and `{{token}}`.
- Consider adding Flyway/Liquibase for schema versioning (future improvement).

## License
Currently no explicit license. Add one (e.g., MIT) if you intend to open-source.

---
## Demo Video

`[Watch the Demo](https://example.com/demo-placeholder)`

---
