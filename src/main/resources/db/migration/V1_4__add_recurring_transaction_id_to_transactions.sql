ALTER TABLE transactions
    ADD COLUMN recurring_transaction_id uuid,
    ADD CONSTRAINT fk_recurring_transaction
        FOREIGN KEY (recurring_transaction_id)
            REFERENCES recurring_transactions(id);
