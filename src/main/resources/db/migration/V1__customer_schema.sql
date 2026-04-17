CREATE TABLE customer
(
    customer_id   BIGINT PRIMARY KEY,
    customer_type SMALLINT  NOT NULL CHECK (customer_type IN (1, 2)),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_business
(
    customer_id BIGINT PRIMARY KEY,
    legal_name  TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_individual
(
    customer_id BIGINT PRIMARY KEY,
    first_name  TEXT      NOT NULL,
    middle_name TEXT,
    last_name   TEXT      NOT NULL,
    suffix      TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE customer_business_id_seq AS bigint START WITH 1000000000000000000 MINVALUE 0 INCREMENT BY 1;
CREATE SEQUENCE customer_individual_id_seq AS bigint START WITH 2000000000000000000 MINVALUE 0 INCREMENT BY 1;

CREATE OR REPLACE FUNCTION next_customer_id(p_customer_type TEXT)
RETURNS BIGINT
LANGUAGE plpgsql
VOLATILE
AS
$$
BEGIN
    IF p_customer_type = 'BUSINESS' THEN
        RETURN nextval('customer_business_id_seq');
    ELSIF p_customer_type = 'INDIVIDUAL' THEN
        RETURN nextval('customer_individual_id_seq');
    END IF;
    RAISE EXCEPTION 'Invalid customer_type: %', p_customer_type;
END;
$$;