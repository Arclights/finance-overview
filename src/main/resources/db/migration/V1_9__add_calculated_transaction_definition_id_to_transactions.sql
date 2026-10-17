ALTER TABLE transactions
    ADD COLUMN calculated_transaction_definition_id uuid,
    ADD CONSTRAINT fk_calculated_transaction_definition
        FOREIGN KEY (calculated_transaction_definition_id)
            REFERENCES calculated_transaction_definitions(id);

-- Enforces "at most one generated row per definition per statement" at the DB
-- level, which the recalculation engine relies on for its find-or-create upsert.
CREATE UNIQUE INDEX uq_calculated_transaction_per_statement
    ON transactions (calculated_transaction_definition_id, statement_id)
    WHERE calculated_transaction_definition_id IS NOT NULL;
