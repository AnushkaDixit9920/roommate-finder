# 🏠 RoommateFinder

A roommate matching platform built with Spring Boot. Users register, set their lifestyle preferences, and get matched with compatible roommates using a weighted scoring algorithm.

## Features

- JWT-based authentication with email verification
- Weighted compatibility scoring across 6 parameters (budget, sleep schedule, cleanliness, noise level, pets, smoking)
- Match request lifecycle — send, accept, reject
- Email notifications on match request and acceptance
- Vanilla JS frontend served as Spring Boot static resource

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Java 21 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | MySQL 8 + Spring Data JPA + Hibernate |
| Email | Spring Mail (Gmail SMTP) |
| Frontend | HTML5, CSS3, Vanilla JS |
| Build | Maven |

## Setup

### 1. Prerequisites
- Java 21
- MySQL 8
- Maven

### 2. Database
Create a MySQL database (or let Spring auto-create it):
```sql
CREATE DATABASE roommate_finder;
```

### 3. Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
```

> For Gmail: Go to Google Account → Security → 2-Step Verification → App Passwords → Generate one for "Mail"

### 4. Run
```bash
mvn spring-boot:run
```

Visit: http://localhost:8080

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login, returns JWT |
| GET | /api/auth/verify?token= | Verify email |

### Profiles
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/profiles | Create/update profile |
| GET | /api/profiles/me | Get my profile |
| GET | /api/profiles/matches | Get matches sorted by score |
| GET | /api/profiles/{userId} | Get any user's profile |

### Matches
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/matches/send/{receiverId} | Send match request |
| POST | /api/matches/{id}/accept | Accept request |
| POST | /api/matches/{id}/reject | Reject request |
| GET | /api/matches/sent | My sent requests |
| GET | /api/matches/received | Requests I received |
| GET | /api/matches/accepted | All accepted matches |

## Compatibility Scoring Algorithm

Scores two profiles on 0–100 scale:

| Parameter | Weight | Logic |
|---|---|---|
| Budget | 30 pts | Full score if ranges overlap, partial if within 10-20% |
| Sleep Schedule | 20 pts | Exact match = 20, FLEXIBLE = 12, mismatch = 0 |
| Cleanliness | 20 pts | Exact = 20, one step apart = 10 |
| Noise Level | 15 pts | Exact = 15, one step apart = 8 |
| Pets | 10 pts | Both same = 10 |
| Smoking | 5 pts | Both same = 5 |

## Project Structure

```
src/main/java/com/roommatefinder/
├── config/          # Security config, exception handler
├── controller/      # REST + page controllers
├── dto/             # Request/response objects
├── entity/          # JPA entities (User, Profile, MatchRequest)
├── repository/      # Spring Data JPA repos
├── security/        # JWT util + filter
└── service/         # Business logic (Auth, Profile, Match, Email, Compatibility)
```
