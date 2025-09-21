# GuestFind

## Project Description

GuestFind is a hotel-focused Lost & Found and Guest Engagement platform. It helps hotels record lost items, register found items, match them, allow guests to submit claims, and coordinate delivery once ownership is validated. The system uses role-based permissions (ADMIN, STAFF, GUEST) and provides notifications and statistics to improve guest satisfaction.

> Centralized lost & found lifecycle: **Report → Match → Claim → Approve → Deliver**

---

## Screenshots

| Feature              | Screenshot                                                            |
| -------------------- | --------------------------------------------------------------------- |
| Dashboard            | ![Dashboard](https://i.ibb.co/sdZdyW5L/Dashboard.png)                 |
| Lost Item Submission | ![Lost Item Form](https://i.ibb.co/Qv6b6NpJ/Lost-Item-Submission.png) |
| Found Items List     | ![Found Items](https://i.ibb.co/pmpbK4w/Found-Items-List.png)         |
| Claims Management    | ![Claims](https://i.ibb.co/5ghtPccw/Claims-Management.png)            |
| Notifications Center | ![Notifications](https://i.ibb.co/4nK83BVq/Notifications-Center.png)  |

---

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

## Setup Instructions

### Prerequisites

* Java 17
* Maven 3.9+
* MySQL 8+

### Backend Setup

```bash
git clone https://github.com/GamithaGimhana/GuestFind
cd GuestFind/backend
```

1. Update `application.properties` with your DB credentials and a secure `jwt.secretKey`.
2. Create the database:

```sql
CREATE DATABASE guestfind;
```

3. Build & run:

```bash
mvn clean install
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

### Default Admin User

```
Email: admin@guestfind.com
Password: admin123
```

### Frontend

Located in `frontend/`.
Simply open `index.html` in your browser.

---

## Demo Video

📺 [Watch the Demo](https://youtu.be/S03Ys5PPbVU)

---

## Tech Stack

* Java 17, Spring Boot 3.5.5
* Spring Security + JWT
* Spring Data JPA (MySQL)
* Maven build
* Simple static HTML/CSS/JS frontend

---

## License

No license specified yet.

---
