---
name: start
description: Start the full SynFlow stack — reuses existing PostgreSQL (5435) and Valkey/Redis (6379), starts Spring Boot API (8089) and Next.js frontend (3010). One command to bring everything up.
user_invocable: true
---

# Start SynFlow

Brings up the entire stack: database, cache, backend API (8089), and frontend (3010).
Reuses existing shared infrastructure (cosmos-postgres on 5435, Valkey on 6379).

## Steps

### 1. Check what's already running

```bash
docker ps --format "{{.Names}} {{.Ports}} {{.Status}}" 2>/dev/null
lsof -i:5435 -i:6379 -i:8089 -i:3010 -sTCP:LISTEN 2>/dev/null
```

Note which services are already up to avoid starting duplicates.

### 2. Ensure PostgreSQL is available (port 5435)

SynFlow reuses the existing `cosmos-postgres` container on port 5435. Do NOT start a separate synflow-postgres — port 5432 is occupied.

Check if the `synflow` database and user exist:

```bash
docker exec cosmos-postgres psql -U postgres -c "SELECT datname FROM pg_database WHERE datname='synflow';"
```

If the database doesn't exist, create it:

```bash
docker exec cosmos-postgres psql -U postgres -c "CREATE USER synflow WITH PASSWORD 'synflow';" 2>/dev/null || true
docker exec cosmos-postgres psql -U postgres -c "CREATE DATABASE synflow OWNER synflow;" 2>/dev/null || true
docker exec cosmos-postgres psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE synflow TO synflow;"
```

### 3. Ensure Redis is available (port 6379)

Valkey on port 6379 is Redis-compatible. If it's already running, skip. If not:

```bash
docker start valkey 2>/dev/null || true
```

### 4. Start Spring Boot backend (port 8089)

Skip if port 8089 is already listening.

Run in background, pointing at postgres on 5435:

```bash
cd /home/t0266li/Documents/SynFlow/synflow-api
CORS_ORIGINS=http://localhost:3010 mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8089 --spring.datasource.url=jdbc:postgresql://localhost:5435/synflow"
```

Wait for "Started SynFlowApplication" in output before proceeding.

### 5. Verify backend

```bash
curl -sf -o /dev/null -w "%{http_code}" -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@synflow.com","password":"admin123"}'
```

Expected: `200`.

### 6. Ensure .env.local points at correct ports

Check `synflow-web/.env.local` and ensure it has the correct URLs. The `.env.local` file overrides environment variables, so it MUST match:

```bash
cd /home/t0266li/Documents/SynFlow/synflow-web
```

Ensure `.env.local` contains:
```
NEXT_PUBLIC_API_URL=http://localhost:8089
NEXTAUTH_URL=http://localhost:3010
```

If the values are wrong (e.g., port 8080 or 3000), fix them before starting the frontend.

### 7. Start Next.js frontend (port 3010)

Skip if port 3010 is already listening.

Use nvm to get a modern Node version since system Node (v12) is too old:

```bash
cd /home/t0266li/Documents/SynFlow/synflow-web
export NVM_DIR="$HOME/.nvm" && [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh" && nvm use 20
```

Install deps if `node_modules` is missing, using `--legacy-peer-deps` to handle eslint peer conflict:

```bash
[ -d node_modules ] || npm install --legacy-peer-deps
```

Start dev server on port 3010:

```bash
npm run dev -- -p 3010
```

### 8. Report status

Show the user:
- PostgreSQL: `localhost:5435` (cosmos-postgres, shared)
- Redis: `localhost:6379` (Valkey)
- Backend API: `http://localhost:8089`
- Frontend: `http://localhost:3010`
- Admin login: `admin@synflow.com` / `admin123`
- User login: `user@synflow.com` / `user123`
