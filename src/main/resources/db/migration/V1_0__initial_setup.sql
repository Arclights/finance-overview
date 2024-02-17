CREATE TYPE month_enum AS ENUM
(
 'JANUARY',
 'FEBRUARY',
 'MARCH',
 'APRIL',
 'MAY',
 'JUNE',
 'JULY',
 'AUGUST',
 'SEPTEMBER',
 'OCTOBER',
 'NOVEMBER',
 'DECEMBER'
);

CREATE TYPE transaction_type AS ENUM
(
  'Income',
  'Expense'
);

CREATE TABLE statements(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    "month" month_enum NOT NULL,
    "year" INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transactions(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    statement_id uuid,
    "date" date NOT NULL,
    type transaction_type NOT NULL,
    amount NUMERIC NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_statement
        FOREIGN KEY(statement_id)
            REFERENCES statements(id)
);

CREATE TABLE category_types(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE categories(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR NOT NULL,
    category_type_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_category_type
        FOREIGN KEY(category_type_id)
            REFERENCES category_types(id)
);

CREATE TABLE transaction_categories(
    transaction_id uuid,
    category_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY(transaction_id, category_id),
    CONSTRAINT fk_transaction
        FOREIGN KEY(transaction_id)
            REFERENCES transactions(id),
    CONSTRAINT fk_category
        FOREIGN KEY(category_id)
            REFERENCES categories(id)
);

CREATE TABLE external_sources(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE external_labels(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    external_source_id uuid,
    name VARCHAR NOT NULL,
    category_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_external_source
        FOREIGN KEY(external_source_id)
            REFERENCES external_sources(id),
    CONSTRAINT fk_category
        FOREIGN KEY(category_id)
            REFERENCES categories(id)
);

CREATE TABLE recurring_transactions(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    type transaction_type NOT NULL,
    amount NUMERIC NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE recurring_transaction_categories(
    recurring_transaction_id uuid,
    category_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY(recurring_transaction_id, category_id),
    CONSTRAINT fk_recurring_transaction
        FOREIGN KEY(recurring_transaction_id)
            REFERENCES recurring_transactions(id),
    CONSTRAINT fk_category
        FOREIGN KEY(category_id)
            REFERENCES categories(id)
);

CREATE OR REPLACE FUNCTION update_updated_column()
    RETURNS TRIGGER AS $$
    BEGIN
        NEW.updated_at = now();
        RETURN NEW;
    END;
    $$ language 'plpgsql';

CREATE TRIGGER update_updated_statements BEFORE UPDATE ON statements FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_transactions BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_category_types BEFORE UPDATE ON category_types FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_categories BEFORE UPDATE ON categories FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_transaction_categories BEFORE UPDATE ON transaction_categories FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_external_sources BEFORE UPDATE ON external_sources FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_external_labels BEFORE UPDATE ON external_labels FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_recurring_transactions BEFORE UPDATE ON recurring_transactions FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
CREATE TRIGGER update_updated_recurring_transaction_categories BEFORE UPDATE ON recurring_transaction_categories FOR EACH ROW EXECUTE PROCEDURE update_updated_column();