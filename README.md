# api-account

Quarkus REST API for account-related data (customers, and more to come).

## Configuration

Main configuration lives in `src/main/resources/application.yaml` (profiles: `%dev`, `%test`, `%prod`).

- **Development**: Dev Services starts PostgreSQL; Flyway runs migrations plus `db/testdata` (see Flyway below). HTTP Basic auth is enabled with embedded user `dev` / `dev` and roles `CUSTOMER_READ`, `CUSTOMER_WRITE`.
- **Tests**: Same Flyway locations as dev; HTTP Basic is off and `io.quarkus.test.security.TestSecurity` supplies roles.
- **Production**: Set `DB_USERNAME`, `DB_PASSWORD`, and optionally `DB_HOST`, `DB_PORT`, `DB_NAME`. Only `db/migration` is used (no test data). Configure OIDC or another identity provider for real deployments; role names should align with `CUSTOMER_READ` / `CUSTOMER_WRITE`.

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

## Customers API

Base path: `/customers`

| Method | Path | Roles |
|--------|------|--------|
| GET | `/customers` | `CUSTOMER_READ` |
| POST | `/customers` | `CUSTOMER_WRITE` |
| GET | `/customers/{customerId}` | `CUSTOMER_READ` |
| PUT | `/customers/{customerId}` | `CUSTOMER_WRITE` |
| DELETE | `/customers/{customerId}` | `CUSTOMER_WRITE` |

**Create body** (`CreateCustomerRequest`): set `customerType` to `INDIVIDUAL` or `BUSINESS` and include exactly one of `individual` or `business`:

- `INDIVIDUAL`: `individual` with `firstName`, `lastName`, optional `email`
- `BUSINESS`: `business` with `legalName`

**Update body** (`UpdateCustomerRequest`): send the payload that matches the existing customer type (`individual` or `business` only).

OpenAPI document: `/openapi` (Swagger UI: `/q/swagger-ui` when enabled).

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
