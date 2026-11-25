# fundamental — Auth API (Spring Boot 4 · Hexagonal)

Stateless auth service: register, login, JWT access token (RS256), refresh token rotation via HttpOnly cookie, logout / logout-all.

| Stack | Versi |
|---|---|
| Java | 21 |
| Spring Boot | 4.2.0 (WebMVC, Security, OAuth2 Resource Server, Data JPA, Flyway, Validation) |
| DB | PostgreSQL 17 |
| Runtime | Docker Compose, virtual threads on |

---

## Quick start (Docker)

### 1. Generate RSA key pair (sekali aja)

Key dipakai buat sign/verify JWT. Folder `keys/` di-ignore git & di-mount read-only ke container (`/run/keys`). Jalankan di **Git Bash**:

```bash
mkdir -p keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/private.pem
openssl rsa -in keys/private.pem -pubout -out keys/public.pem
```

> Format harus PKCS#8 (`-----BEGIN PRIVATE KEY-----`). `genpkey` udah otomatis PKCS#8.

### 2. Run

```bash
docker compose up --build        # foreground, log langsung keliatan
docker compose up --build -d     # background
docker compose watch             # auto-rebuild tiap src/ atau pom.xml berubah
```

App siap kalau log muncul `Started FundamentalApplication`. Flyway otomatis jalanin migration `V1` & `V2`.

### 3. Port

| Service | Dari host (Windows/browser/Postman) | Dari container lain di network compose |
|---|---|---|
| API | **http://localhost:8080** | `http://app:8080` |
| PostgreSQL | `localhost:5432` — db `auth`, user `auth`, pass `auth` | `postgres:5432` |

Client yang jalan di container lain **di luar** compose ini → pakai `http://host.docker.internal:8080`.

### Stop / reset

```bash
docker compose down        # stop, data DB tetap
docker compose down -v     # stop + hapus volume pgdata (DB fresh)
```

---

## API

Semua error pakai format RFC 9457 `ProblemDetail` + field `code` & `timestamp`.

| Method | Path | Auth | Body / Input | Sukses |
|---|---|---|---|---|
| POST | `/api/auth/register` | public | `{"username","password"}` | `201` `{id, username, role}` |
| POST | `/api/auth/login` | public | `{"username","password"}` | `200` `{accessToken, tokenType, expiresIn}` + `Set-Cookie: refreshToken` |
| POST | `/api/auth/refresh` | cookie `refreshToken` | — | `200` token baru + cookie baru (rotation) |
| POST | `/api/auth/logout` | cookie `refreshToken` | — | `204` + cookie di-clear |
| POST | `/api/auth/logout-all` | `Bearer` | — | `204`, semua refresh token user di-revoke |
| GET | `/api/me` | `Bearer` | — | `200` `{id, username, roles}` |

**Validasi register**: username 4–50 char `[A-Za-z0-9._-]` (disimpan lowercase + trim), password 8–72 char (limit bcrypt).

**TTL**: access token 15m, refresh token 7d (`jwt.access-ttl`, `jwt.refresh-ttl` di `application.yml`).

### Error codes

| HTTP | `code` | Kapan |
|---|---|---|
| 400 | — | Validasi body gagal |
| 401 | `AUTH-002` | Username/password salah |
| 401 | `AUTH-003` | Refresh token kosong / invalid / expired / revoked / reuse |
| 401 | `UNAUTHENTICATED` | Bearer token gak ada / invalid / expired |
| 403 | `ACCESS_DENIED` | Role kurang |
| 409 | `AUTH-001` | Username udah dipakai |
| 500 | `INTERNAL_ERROR` | Unhandled exception (detail cuma di log server) |

### Test pakai curl (Git Bash)

> PowerShell: `curl` = alias `Invoke-WebRequest`. Pakai Git Bash, atau panggil `curl.exe`.

```bash
BASE=http://localhost:8080

# register
curl -i -X POST $BASE/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"lukman","password":"password123"}'

# login → simpan cookie refreshToken ke jar.txt, ambil accessToken
TOKEN=$(curl -s -c jar.txt -X POST $BASE/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"lukman","password":"password123"}' \
  | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

# endpoint protected
curl -i $BASE/api/me -H "Authorization: Bearer $TOKEN"

# refresh (rotation: cookie lama di-revoke, jar.txt ke-update cookie baru)
curl -i -b jar.txt -c jar.txt -X POST $BASE/api/auth/refresh

# logout satu sesi / semua sesi
curl -i -b jar.txt -c jar.txt -X POST $BASE/api/auth/logout
curl -i -X POST $BASE/api/auth/logout-all -H "Authorization: Bearer $TOKEN"
```

**Note cookie**: `refreshToken` itu `HttpOnly; Secure; SameSite=Strict; Path=/api/auth`. Di `http://localhost` tetap jalan (curl ≥ 7.79 & browser anggap localhost secure). Kalau client lain (Postman, dll.) gak mau kirim cookie `Secure` lewat http, kirim manual: `-H "Cookie: refreshToken=<value>"`.

**Cek reuse detection**: simpan cookie lama, refresh sekali, lalu refresh lagi pakai cookie **lama** → `401 AUTH-003` + semua sesi user di-revoke (log `WARN Refresh token reuse detected`).

---

## Arsitektur (Hexagonal / Ports & Adapters)

```
src/main/java/com/lukmn/fundamental
├── domain/                    # Pure Java, zero framework
│   ├── model/                 # User, RefreshToken, Role
│   └── exception/             # BusinessException, ErrorCode
├── application/
│   ├── port/in/               # Use case interface: Register/Login/RefreshToken/Logout + AuthTokens
│   ├── port/out/              # Kebutuhan ke luar: UserRepository, RefreshTokenRepository, PasswordHasher, AccessTokenIssuer
│   ├── service/               # AuthService (implement semua port/in), TokenTtl
│   └── support/               # OpaqueTokens (generate + SHA-256)
├── adapter/
│   ├── in/web/                # Controller, DTO, GlobalExceptionHandler, SecurityProblemHandler, RefreshTokenCookie
│   └── out/
│       ├── persistence/       # JPA entity + Spring Data repo + adapter implement port/out
│       └── security/          # JwtAccessTokenIssuer (Nimbus RS256), SpringSecurityPasswordHasher (bcrypt)
└── config/                    # SecurityConfig, UseCaseConfig (wiring AuthService), JwtProperties, RsaKeyProperties
```

Aturan dependency: `adapter → application → domain`. Domain & application gak kenal Spring (kecuali `@Transactional`). `AuthService` gak pakai `@Service`, di-wire manual di `UseCaseConfig`.

DB: tabel `users` & `refresh_tokens` (Flyway, `src/main/resources/db/migration`). Refresh token cuma disimpan **hash SHA-256**-nya, raw value cuma ada di cookie client.

---

## Cara trace

### A. Trace alur kode per request

```
HTTP
 │
 ▼
SecurityFilterChain ── Bearer? → JwtDecoder (public key + validasi issuer) → gagal? → SecurityProblemHandler → GlobalExceptionHandler (401/403)
 │
 ▼
adapter/in/web  (Controller → bikin Command dari DTO)
 │  port/in  (interface UseCase)
 ▼
application/service/AuthService
 │  port/out (interface)
 ├──► UserPersistenceAdapter / RefreshTokenPersistenceAdapter → Spring Data JPA → PostgreSQL
 ├──► SpringSecurityPasswordHasher (bcrypt)
 └──► JwtAccessTokenIssuer (NimbusJwtEncoder, private key)
 │
 ▼
BusinessException? → GlobalExceptionHandler → ProblemDetail
```

| Endpoint | Jalur |
|---|---|
| `register` | `AuthController.register` → `AuthService.register` → `User.normalizeUsername` → `UserRepository.existsByUsername` → `PasswordHasher.hash` → `UserRepository.save` (unique constraint race → `AUTH-001`) |
| `login` | `AuthController.login` → `AuthService.login` → `findByUsername` → `PasswordHasher.matches` (user gak ada tetap hash dummy, anti timing attack) → `issueTokens`: `AccessTokenIssuer.issue` + `OpaqueTokens.generate` → simpan `sha256Hex` ke `refresh_tokens` → `Set-Cookie` |
| `refresh` | `@CookieValue refreshToken` → `AuthService.refresh` → `findByTokenHash` → `isExpired` → `revokeIfActive` (atomic `UPDATE ... WHERE revoked_at IS NULL`) → gagal = reuse → `revokeAllActiveByUserId` + 401 · sukses → `issueTokens` (pair baru) |
| `logout` | `AuthService.logout` → `revokeIfActive` → cookie di-clear |
| `logout-all` | Bearer → `jwt.getSubject()` (user id) → `revokeAllActiveByUserId` |
| `/api/me` | Bearer → baca claim `sub`, `username`, `roles` dari JWT (gak hit DB) |

**Tips IntelliJ**: di interface port (`LoginUseCase`, `UserRepository`, dll.) tekan `Ctrl+Alt+B` → lompat ke implementasi. Taruh breakpoint di `AuthService` buat lihat seluruh use case dalam satu tempat.

### B. Trace runtime — log

```bash
docker compose logs -f app          # follow log app
docker compose logs -f              # app + postgres
```

Naikin log level tanpa ubah kode → bikin `compose.override.yml` (otomatis di-merge sama `compose.yml`, jangan di-commit):

```yaml
services:
  app:
    environment:
      LOGGING_LEVEL_ORG_HIBERNATE_SQL: DEBUG                 # SQL yang dieksekusi
      LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND: TRACE       # nilai parameter SQL
      LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY: DEBUG      # filter chain, hasil auth, Granted Authorities
      LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB: DEBUG           # mapping request → controller, response status
      LOGGING_LEVEL_COM_LUKMN_FUNDAMENTAL: DEBUG             # log app sendiri
```

Lalu `docker compose up -d` (container di-recreate otomatis).

> `ORG_HIBERNATE_ORM_JDBC_BIND=TRACE` nampilin semua parameter termasuk password hash & token hash. Lokal aja.

### C. Trace runtime — debugger (breakpoint di container)

Tambahin ke `compose.override.yml` yang sama:

```yaml
services:
  app:
    environment:
      JAVA_TOOL_OPTIONS: "-XX:MaxRAMPercentage=75 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
    ports:
      - "5005:5005"
```

`docker compose up -d` → IntelliJ: **Run → Edit Configurations → + → Remote JVM Debug**, host `localhost`, port `5005` → Debug. Breakpoint di source lokal langsung kena (source harus sama dengan image yang jalan).

### D. Trace data — DB & JWT

```bash
docker compose exec postgres psql -U auth -d auth
```

```sql
SELECT id, username, role, created_at FROM users;
SELECT id, user_id, left(token_hash, 12) AS hash, expires_at, revoked_at FROM refresh_tokens ORDER BY id DESC;
SELECT version, description, success FROM flyway_schema_history;
```

Decode payload JWT (cek `sub`, `iss`, `roles`, `exp`):

```bash
echo "$TOKEN" | cut -d. -f2 | tr '_-' '/+' | base64 -d 2>/dev/null; echo
```

### E. Distributed tracing

Belum dipasang (single service, belum perlu). Kalau nanti ada service kedua: tambah `spring-boot-starter-actuator` + Micrometer Tracing (OpenTelemetry bridge) + exporter OTLP ke Jaeger/Tempo.

---

## Run tanpa Docker (app di IDE, DB tetap di Docker)

```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/auth --spring.datasource.username=auth --spring.datasource.password=auth --rsa.private-key=file:keys/private.pem --rsa.public-key=file:keys/public.pem"
```

Di IntelliJ: argumen yang sama taruh di **Program arguments** run config `FundamentalApplication`. Matiin service `app` di Docker dulu biar port 8080 gak bentrok (`docker compose stop app`).
