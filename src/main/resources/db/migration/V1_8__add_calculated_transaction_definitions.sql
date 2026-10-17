CREATE TABLE calculated_transaction_definitions(
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR NOT NULL,
    variable_a_id uuid NOT NULL,
    variable_b_id uuid NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_variable_a FOREIGN KEY (variable_a_id) REFERENCES variables(id),
    CONSTRAINT fk_variable_b FOREIGN KEY (variable_b_id) REFERENCES variables(id)
);

CREATE TABLE calculated_transaction_definition_taxonomies(
    calculated_transaction_definition_id uuid,
    taxonomy_id uuid,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (calculated_transaction_definition_id, taxonomy_id),
    CONSTRAINT fk_definition FOREIGN KEY (calculated_transaction_definition_id)
        REFERENCES calculated_transaction_definitions(id),
    CONSTRAINT fk_taxonomy FOREIGN KEY (taxonomy_id) REFERENCES taxonomies(id)
);

CREATE TRIGGER update_updated_calculated_transaction_definitions
    BEFORE UPDATE ON calculated_transaction_definitions
    FOR EACH ROW EXECUTE PROCEDURE update_updated_column();
