-- BUSINESS -> 1000000000000000000; INDIVIDUAL -> 2000000000000000000 (separate sequences)
WITH c AS (
    INSERT INTO customer (customer_id, customer_type, created_at, updated_at)
        VALUES (next_customer_id('BUSINESS'), 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING customer_id)
INSERT
INTO customer_business (customer_id, legal_name, created_at, updated_at)
SELECT customer_id, 'Acme Example Corp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM c;

WITH c AS (
    INSERT INTO customer (customer_id, customer_type, created_at, updated_at)
        VALUES (next_customer_id('INDIVIDUAL'), 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        RETURNING customer_id)
INSERT
INTO customer_individual (customer_id, first_name, middle_name, last_name, suffix, created_at, updated_at)
SELECT customer_id, 'Test', NULL, 'Individual', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM c;
