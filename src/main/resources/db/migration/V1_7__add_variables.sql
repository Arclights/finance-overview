CREATE TYPE variable_type AS ENUM ('Static', 'Average', 'CurrentPeriodSum');

CREATE TABLE variables(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR NOT NULL,
    type variable_type NOT NULL,
    static_value NUMERIC,
    recurring_transaction_id uuid,
    window_size INTEGER,
    fallback_average NUMERIC,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_recurring_transaction
        FOREIGN KEY (recurring_transaction_id) REFERENCES recurring_transactions(id)
);

CREATE TRIGGER update_updated_variables BEFORE UPDATE ON variables
    FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
