# Trouble Ticket Service

Implementacja REST API zgodna z profilem TMF621 Trouble Ticket, zrealizowana jako zadanie rekrutacyjne dla Netia S.A.

---

## Decyzje techniczne

### Stack

| Warstwa | Technologia |
|---|---|
| Język | Java 21 (virtual threads ready) |
| Framework | Spring Boot 3.4.5 |
| Baza danych | PostgreSQL 16 |
| Migracje | Flyway |
| Bezpieczeństwo | Spring Security + OAuth2 Resource Server (JWT Bearer) |
| Testy integracyjne | Testcontainers (PostgreSQL) |
| Testy BDD | Cucumber 7 + JUnit Platform Suite |
| Konteneryzacja | Docker + Docker Compose |

### Architektura

Projekt stosuje **Hexagonal Architecture (Ports & Adapters)**:

```
domain/
  model/          – czyste rekordy domenowe (TroubleTicket, Note, TroubleTicketStatus)
  port/in/        – interfejsy use case (CreateTroubleTicketUseCase, CloseTroubleTicketUseCase, …)
  port/out/       – interfejs repozytorium (TroubleTicketRepository)
  service/        – TroubleTicketService – jedyna logika biznesowa

infrastructure/
  adapter/in/web/       – REST kontroler, DTO, GlobalExceptionHandler
  adapter/out/persistence/ – JPA encje, Spring Data repozytoria, adapter
  config/               – SecurityConfig (stateless JWT)

shared/exception/  – wyjątki domenowe
```

Domena **nie zna** Springa, JPA ani HTTP — zależy wyłącznie od interfejsów portów.

### Kluczowe decyzje

**Idempotencja przy tworzeniu zgłoszenia**
Unikalny klucz `(tenantId, externalId)` na poziomie bazy danych. Przy powtórnym żądaniu z tym samym kluczem API zwraca `200 OK` z istniejącym zasobem zamiast `201 Created`.

**Tenant scope wyłącznie z JWT**
`tenantId` pochodzi z `sub` claimu tokenu. Żaden parametr HTTP nie może nadpisać tej wartości. Zasób niewidoczny w tenant scope zwraca `404` (nie `403`) — nie ujawniamy istnienia zasobu.

**Status przy tworzeniu**
Klient może wysłać wyłącznie `status: "new"`. Serwis zapisuje zgłoszenie ze statusem `acknowledged`, symulując natychmiastowe potwierdzenie przez SOZ.

**Publiczny PATCH — tylko `closed`**
Walidacja na poziomie DTO (`@Pattern(regexp = "closed")`). Każda inna wartość zwraca `400 VALIDATION_ERROR`.

**Mapowanie statusów**
Enum domenowy (`TroubleTicketStatus`) jest mapowany do wartości API (np. `IN_PROGRESS` → `"inProgress"`) w warstwie web — domena nie zna formatu API.

**Walidacja `serviceId` — przyjęte założenie**
Kontrakt OpenAPI definiuje odpowiedź `404 ServiceNotFound` dla `POST /troubleTicket`, sugerując weryfikację istnienia usługi w zewnętrznym rejestrze. W v1 przyjęto upraszczające założenie: `serviceId` jest traktowane jako dowolna liczba całkowita ≥ 1 i nie jest walidowane względem rejestru usług. Założenie to wynika z braku specyfikacji rejestru w zadaniu. W środowisku produkcyjnym należałoby dodać port wyjściowy `ServiceRegistry` i jego adapter (np. REST call do systemu CRM/OSS).

**Brak paginacji w v1**
Zgodnie z kontraktem OpenAPI — `GET /troubleTicket` zwraca pełną listę w tenant scope bez filtrowania.

---

## Wymagania

- Java 21+
- Maven 3.9+
- Docker + Docker Compose

---

## Uruchomienie lokalne

### 1. Tylko baza danych (developerski tryb)

```bash
docker compose up postgres -d
```

Następnie uruchom aplikację przez IDE lub Maven:

```bash
mvn spring-boot:run
```

> Wymagane zmienne środowiskowe (lub domyślne z `application.yml`):
> - `JWT_ISSUER_URI` — adres issuer JWT (np. Keycloak realm)

### 2. Pełny stack (aplikacja + baza)

```bash
docker compose up --build
```

Aplikacja dostępna pod: `http://localhost:8080/api/v1/troubleTicket`

---

## Testy

### Uruchomienie wszystkich testów (BDD + integracyjne)

```bash
mvn test
```

Testcontainers automatycznie startuje instancję PostgreSQL na potrzeby testów — Docker musi być uruchomiony.

### Raport Cucumber

Po uruchomieniu testów raport JSON dostępny w:

```
target/cucumber-reports/cucumber.json
```

### Struktura testów BDD

```
src/test/resources/features/
  trouble_ticket.feature   – 11 scenariuszy pokrywających wszystkie endpointy

src/test/java/pl/netia/troubleticket/bdd/
  CucumberTest.java                  – runner JUnit Platform Suite
  CucumberSpringConfiguration.java   – @SpringBootTest + Testcontainers
  TestJwtUtils.java                  – generator testowych JWT (RSA-256)
  TestSecurityConfig.java            – testowy JwtDecoder (klucz RSA in-memory)
  steps/TroubleTicketSteps.java      – definicje kroków Cucumber
```

---

## Endpointy API

Wszystkie endpointy wymagają nagłówka `Authorization: Bearer <token>`.

| Metoda | Ścieżka | Opis |
|---|---|---|
| `POST` | `/api/v1/troubleTicket` | Utwórz zgłoszenie (idempotentne) |
| `GET` | `/api/v1/troubleTicket` | Lista zgłoszeń w tenant scope |
| `GET` | `/api/v1/troubleTicket/{id}` | Pobierz zgłoszenie |
| `PATCH` | `/api/v1/troubleTicket/{id}` | Zamknij zgłoszenie (`status: "closed"`) |
| `POST` | `/api/v1/troubleTicket/{id}/note` | Dodaj notatkę |

Pełna specyfikacja: [`trouble-ticket-api.yaml`](../trouble-ticket-api.yaml)

---

## Struktura branchy

```
develop
├── feature/ports               – model domenowy + interfejsy use case
├── feature/persistence-adapter – JPA entities + adapter
├── feature/application         – TroubleTicketService
├── feature/rest-adapter        – kontroler REST, DTO, security
├── feature/docker              – Dockerfile, docker-compose
└── feature/bdd-tests           – scenariusze Cucumber + konfiguracja testów
```
