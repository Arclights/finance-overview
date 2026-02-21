CREATE TABLE statement_persons(
    statement_id uuid,
    category_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(statement_id, category_id),
    CONSTRAINT fk_statement
        FOREIGN KEY(statement_id)
            REFERENCES statements(id),
    CONSTRAINT fk_category
        FOREIGN KEY(category_id)
            REFERENCES categories(id)
);
