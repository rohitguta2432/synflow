---
name: stop-all
description: Stop all SynFlow services — kill backend (8089), frontend (3010) processes and tear down Docker containers. Use when stopping services, shutting down, or cleaning up.
user_invocable: true
---

# Stop All SynFlow Services

Cleanly shut down the entire SynFlow stack.

## Steps

### 1. Kill the Spring Boot process (if running locally)

```bash
pkill -f "synflow-api" 2>/dev/null || true
lsof -ti:8089 | xargs kill -9 2>/dev/null || true
```

### 2. Kill the Next.js dev server (if running locally)

```bash
lsof -ti:3010 | xargs kill -9 2>/dev/null || true
```

### 3. Stop SynFlow Docker containers

Only stop SynFlow's own containers — don't touch other running containers (cosmos, kafka, temporal, etc.):

```bash
cd /home/t0266li/Documents/SynFlow
docker compose down
```

### 4. Confirm everything is stopped

```bash
docker compose ps
lsof -i:8089 2>/dev/null || echo "Port 8089 free"
lsof -i:3010 2>/dev/null || echo "Port 3010 free"
```

### 5. Report that all services are stopped

## Note
- `docker compose down` stops containers but preserves the PostgreSQL volume (data persists).
- To also wipe the database: `docker compose down -v`
- This does NOT stop non-SynFlow containers (Valkey, Kafka, Temporal, etc.)
