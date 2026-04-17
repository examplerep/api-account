# api-account

Quarkus REST API for account-related data (customers, and more to come).

## Configuration

Main configuration lives in `src/main/resources/application.yaml` (profiles: `%dev`, `%test`, `%prod`).

### Keycloak (OIDC)

Authentication uses a **separate Keycloak** (or any OIDC) server. The app is an **OIDC service** (`quarkus.oidc.application-type=service`): it validates **Bearer access tokens** using the realm’s OpenID discovery document and JWKS (no local PEM keys).

- **`KEYCLOAK_SERVER_URL`**: Keycloak base URL (no path), e.g. `http://localhost:8180` (default avoids clashing with Quarkus on port 8080).
- **`KEYCLOAK_REALM`**: realm name (default `api-account`).
- **`KEYCLOAK_CLIENT_ID`**: client id (default `api-account`).
- **Roles**: Endpoints expect `CUSTOMER_READ` / `CUSTOMER_WRITE`. Quarkus OIDC maps **Keycloak realm roles** from `realm_access.roles` automatically (and can use `resource_access.<client-id>.roles` when applicable). If you use a **groups** mapper instead, set `quarkus.oidc.roles.role-claim-path=groups` under `quarkus.oidc` (see Quarkus OIDC docs).

**Keycloak setup (summary)**

1. Create a realm (or use `KEYCLOAK_REALM`).
2. Create a **client** (e.g. `api-account`) for your SPA or machine clients; enable **Bearer-only** access as needed for APIs that only validate tokens.
3. Add **realm roles** `CUSTOMER_READ` and `CUSTOMER_WRITE` and assign them to users or service accounts (or use a protocol mapper to map roles into `groups` if you prefer that claim path).

**Development**

- Dev Services starts PostgreSQL; Flyway runs migrations plus `db/testdata`.
- Run Keycloak on the side and point `KEYCLOAK_SERVER_URL` / `KEYCLOAK_REALM` at it. Obtain access tokens from Keycloak (e.g. password grant for a test user, or authorization code from your SPA) and send `Authorization: Bearer <access_token>`.

**Tests**

- The `%test` profile sets `quarkus.oidc.enabled=false` so unit tests do not require Keycloak. `CustomerResourceTest` uses `io.quarkus.test.security.TestSecurity` to exercise the same `@RolesAllowed` rules.
- For integration tests against a real Keycloak, see Quarkus [Dev Services for Keycloak](https://quarkus.io/guides/security-openid-connect-dev-services) or `quarkus-test-keycloak-server`.

**Production**

- Set `DB_USERNAME`, `DB_PASSWORD`, and optionally `DB_HOST`, `DB_PORT`, `DB_NAME`. Only `db/migration` is used (no test data).
- Set `KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM`, and `KEYCLOAK_CLIENT_ID` to your production Keycloak. If issuer URLs differ from internal hostnames, align Keycloak `KEYCLOAK_FRONTEND_URL` / hostname settings with Quarkus [OIDC issuer](https://quarkus.io/guides/security-oidc-bearer-token-authentication) guidance.

## Database and Flyway

- Schema scripts: `src/main/resources/db/migration/`
- Dev/test-only data: `src/main/resources/db/testdata/` (e.g. `V999__testdata.sql`)

Tables: `customer` (`customer_type` as `SMALLINT`: `1` = BUSINESS, `2` = INDIVIDUAL), `customer_individual` (person fields), `customer_business` (business fields).

**Customer IDs** are allocated from PostgreSQL sequences `customer_business_id_seq` (starts at `1000000000000000000`) and `customer_individual_id_seq` (starts at `2000000000000000000`), each incrementing by 1. Use the single function `next_customer_id(customer_type)`:

- `'BUSINESS'` → `nextval('customer_business_id_seq')`
- `'INDIVIDUAL'` → `nextval('customer_individual_id_seq')`

Call `next_customer_id(...)` from SQL or JDBC in the same transaction as inserts into `customer` (and related child rows).

### Hibernate / Panache (repository)

- Package: `com.examplerep.account.repository`
- Entities: `CustomerEntity`, `CustomerIndividualEntity`, `CustomerBusinessEntity` (maps to `customer`, `customer_individual`, `customer_business`)
- `CustomerRepository` (`PanacheRepository<CustomerEntity>`): `nextCustomerId(CustomerType)`, `persistCustomer`, `findByIdWithDetails`, `findAllWithDetails`
- `CustomerTypeConverter`: maps enum `CustomerType` ↔ `SMALLINT` (1 = BUSINESS, 2 = INDIVIDUAL)

### REST (resource layer)

- Package: `com.examplerep.account.api` — `AccountApiApplication` (`@ApplicationPath("/api")`) prefixes all JAX-RS resources; `CustomerResource` maps to `CustomerService`; DTOs `CreateCustomerRequest`, `UpdateCustomerRequest`; OIDC roles via `@RolesAllowed` (`CUSTOMER_READ` / `CUSTOMER_WRITE`)

### Service layer

- Package: `com.examplerep.account.service`
- Domain: `Customer` (id, type, **`name`**, timestamps), `CustomerDetail` (summary plus `individual` or `business`), `CustomerIndividual`, `CustomerBusiness` (payloads for create/update)
- **`Customer.name`**: BUSINESS → `legal_name`; INDIVIDUAL → `firstName + " " + lastName`, and if `suffix` is non-blank → `", " + suffix` (e.g. `Jane Doe, Jr`)
- `CustomerService`: `listCustomers`, `findCustomer`, `findCustomerDetail`, `create` (overloaded: `CustomerIndividual` / `CustomerBusiness`), `update` (overloaded by payload type), `delete` — uses `CustomerRepository`, maps entities ↔ domain, `@Valid` on inputs, `NotFoundException` / `BadRequestException` where appropriate

## Customers API

Base path: `/api/customers` (Jakarta REST application path `/api` + resource `/customers`). Send `Authorization: Bearer <access_token>` on every request.

| Method | Path | Roles |
|--------|------|--------|
| GET | `/api/customers` | `CUSTOMER_READ` |
| POST | `/api/customers` | `CUSTOMER_WRITE` |
| GET | `/api/customers/{customerId}` | `CUSTOMER_READ` |
| PUT | `/api/customers/{customerId}` | `CUSTOMER_WRITE` |
| DELETE | `/api/customers/{customerId}` | `CUSTOMER_WRITE` |

**Create body** (`CreateCustomerRequest`): set `customerType` to `INDIVIDUAL` or `BUSINESS` and include exactly one of `individual` or `business`:

- `INDIVIDUAL`: `individual` with `firstName`, `lastName`, optional `middleName` and `suffix`
- `BUSINESS`: `business` with `legalName`

**Update body** (`UpdateCustomerRequest`): send exactly one of `individual` or `business`, matching the stored customer type.

**Responses**: list and summary use `Customer`; `GET` and mutating responses that return a body use `CustomerDetail` (`customer` plus `individual` or `business` when present).

OpenAPI document: `/openapi` (Swagger UI: `/q/swagger-ui` when enabled). API uses the `bearer-jwt` security scheme (Bearer tokens from Keycloak).

## Running

```shell
./mvnw quarkus:dev
```

Packaging:

```shell
./mvnw package
```

Native (optional):

```shell
./mvnw package -Dnative
```

## Related Guides

- REST Jackson: <https://quarkus.io/guides/rest#json-serialisation>
- YAML Configuration: <https://quarkus.io/guides/config-yaml>
- Hibernate ORM with Panache: <https://quarkus.io/guides/hibernate-orm-panache>
- Flyway: <https://quarkus.io/guides/flyway>
- OpenAPI: <https://quarkus.io/guides/openapi-swaggerui>
- OIDC Bearer token authentication: <https://quarkus.io/guides/security-oidc-bearer-token-authentication>
