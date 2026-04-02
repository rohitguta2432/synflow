# SynFlow — Enterprise Intelligence Platform

Private network intelligence and deal matching platform. Full-stack application with Spring Boot backend and Next.js frontend.

## Architecture

```
synflow-api/       → Spring Boot REST API (Java 21, port 8089)
synflow-web/       → Next.js 14 App Router frontend (TypeScript, port 3010)
docker-compose.yml → PostgreSQL 16 + Redis 7 + API + Web
```

## Tech Stack

**Backend:** Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, PostgreSQL, Redis, Flyway, JaCoCo

**Frontend:** Next.js 14, TypeScript, Tailwind CSS, React Query, React Hook Form, Zod, Recharts, D3.js

**AI:** OpenAI GPT-4o (profile generation)

**Security:** AES-256 encryption for sensitive fields, JWT authentication

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose
- PostgreSQL 16 (or use Docker)
- Redis 7 (or Redis-compatible like Valkey)

### Backend

```bash
cd synflow-api
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8089"
```

### Frontend

```bash
cd synflow-web
npm install --legacy-peer-deps
npm run dev -- -p 3010
```

### Full Stack (Docker)

```bash
docker compose up --build
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `synflow` | Database name |
| `DB_USERNAME` | `synflow` | Database user |
| `DB_PASSWORD` | `synflow` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `SERVER_PORT` | `8080` | Backend server port |
| `CORS_ORIGINS` | `http://localhost:3000` | Allowed CORS origins |
| `JWT_SECRET` | — | JWT signing secret |
| `ENCRYPTION_KEY` | — | AES-256 encryption key |
| `OPENAI_API_KEY` | — | OpenAI API key for profile generation |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Backend API URL for frontend |

## Features

- **Profile Management** — Create, edit, and manage network profiles (REAL and SHADOW types)
- **Deal Tracking** — Track deals across industries with status lifecycle
- **Intelligence Matching** — Rule-based scoring algorithm matching deals to profiles by industry, expertise, and geography
- **AI Profile Generator** — Extract structured profiles from LinkedIn/website text using GPT-4o
- **Dashboard** — Overview stats, recent activity, and quick actions
- **Admin Panel** — User management and system administration
- **AES-256 Encryption** — Sensitive fields encrypted at rest

## API

All endpoints at `/api/**`. Authentication via `Authorization: Bearer <jwt>`.

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/login` | Authenticate and get JWT |
| `POST /api/auth/register` | Register new user |
| `GET/POST /api/profiles` | List/create profiles |
| `GET/POST /api/deals` | List/create deals |
| `POST /api/deals/{id}/match` | Run matching for a deal |
| `GET /api/matches` | List matches with filters |
| `POST /api/ai/generate-profile` | AI profile generation |
| `GET /api/admin/users` | Admin: list users |

## Database

PostgreSQL with Flyway migrations in `synflow-api/src/main/resources/db/migration/`.

## Testing

```bash
cd synflow-api
mvn test
```

JaCoCo enforces 80% minimum code coverage.

## License

Proprietary. All rights reserved.
