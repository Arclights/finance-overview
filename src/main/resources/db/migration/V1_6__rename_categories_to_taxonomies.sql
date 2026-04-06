ALTER TABLE categories RENAME TO taxonomies;
ALTER TABLE category_types RENAME TO taxonomy_types;
ALTER TABLE taxonomies RENAME COLUMN category_type_id TO taxonomy_type_id;
ALTER TABLE taxonomies RENAME COLUMN parent_category_id TO parent_taxonomy_id;

ALTER TABLE transaction_categories RENAME TO transaction_taxonomies;
ALTER TABLE transaction_taxonomies RENAME COLUMN category_id TO taxonomy_id;

ALTER TABLE recurring_transaction_categories RENAME TO recurring_transaction_taxonomies;
ALTER TABLE recurring_transaction_taxonomies RENAME COLUMN category_id TO taxonomy_id;

ALTER TABLE external_labels_categories RENAME TO external_labels_taxonomies;
ALTER TABLE external_labels_taxonomies RENAME COLUMN category_id TO taxonomy_id;

ALTER TABLE statement_persons RENAME COLUMN category_id TO taxonomy_id;
