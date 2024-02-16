# Tables

### Statement

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| month      | enum      |
| year       | int       |
| created_at | timestamp |
| updated_at | timestamp |

### Transaction

| Name        | Type           |
|-------------|----------------|
| id          | uuid           |
| date        | date           |
| statementId | fk             |
| type        | income/expense |
| amount      | decimal        |
| created_at  | timestamp      |
| updated_at  | timestamp      |

### Category Type

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| name       | text      |
| created_at | timestamp |
| updated_at | timestamp |

### Category

| Name           | Type      |
|----------------|-----------|
| id             | uuid      |
| name           | text      |
| categoryTypeId | fk        |
| created_at     | timestamp |
| updated_at     | timestamp |

### Transaction - Category

| Name          | Type      |
|---------------|-----------|
| transactionId | fk        |
| categoryId    | fk        |
| created_at    | timestamp |
| updated_at    | timestamp |

### External Source

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| name       | text      |
| created_at | timestamp |
| updated_at | timestamp |

### External Labels

| Name             | Type      |
|------------------|-----------|
| id               | uuid      |
| externalSourceId | fk        |
| name             | text      |
| categoryId       | fk        |
| created_at       | timestamp |
| updated_at       | timestamp |

### Recurring Transactions

| Name       | Type           |
|------------|----------------|
| id         | uuid           |
| type       | income/expense |
| amount     | decimal        |
| created_at | timestamp      |
| updated_at | timestamp      |

### Recurring Transaction - Category

| Name                   | Type      |
|------------------------|-----------|
| recurringTransactionId | fk        |
| categoryId             | fk        |
| created_at             | timestamp |
| updated_at             | timestamp |

# Endpoints

`/v1/statements`

`/v1/statements/<id>/transactions?page&pageSize&categories&categoryTypes`

`/v1/statements/<id>/transactions/sum`