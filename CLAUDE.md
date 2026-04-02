# SynFlow — Enterprise Intelligence Platform

## Project Overview
Private network intelligence and deal matching platform. Full-stack: Spring Boot 3.4 (Java 21) backend + Next.js 14 (TypeScript) frontend.

## Quick Start
- `/start` — Start the entire stack (PostgreSQL, Redis, API on 8089, UI on 3010)
- `/stop-all` — Tear down all services

## Architecture
```
synflow-api/     → Spring Boot REST API (port 8089)
synflow-web/     → Next.js App Router frontend (port 3010)
docker-compose.yml → PostgreSQL 16 + Redis 7 + API + Web
```

## Key Commands
```bash
# Backend
cd synflow-api && mvn spring-boot:run
cd synflow-api && mvn test           # Run tests with JaCoCo (80% min coverage)

# Frontend
cd synflow-web && npm install && npm run dev

# Docker (full stack)
docker compose up --build
```

## Demo Credentials
- Admin: `admin@synflow.com` / `admin123`
- User: `user@synflow.com` / `user123`

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, PostgreSQL, Redis, Flyway, OpenAI GPT-4o
- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind CSS, React Query, React Hook Form + Zod, Recharts, D3.js
- **Infra**: Docker Compose, JaCoCo (coverage), AES-256 encryption for sensitive fields

## API Base
All endpoints at `/api/**`. Auth via `Authorization: Bearer <jwt>`. Admin-only: DELETE operations + `/api/admin/**`.

## Database
PostgreSQL with Flyway migrations in `synflow-api/src/main/resources/db/migration/`.
