# Tables

### Statement
All the transactions occurring in a month

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| month      | enum      |
| year       | int       |
| created_at | timestamp |
| updated_at | timestamp |

### Transaction
A transaction coupled to a statement. Containing the amount and when it happened

| Name         | Type           | Comment                                                                              |
|--------------|----------------|--------------------------------------------------------------------------------------|
| id           | uuid           |                                                                                      |
| originalName | text           | original name of the transaction. Probably from the external source. Ex. Mathem12342 |
| date         | date           |                                                                                      |
| statementId  | fk             |                                                                                      |
| type         | income/expense |                                                                                      |
| amount       | decimal        |                                                                                      |
| created_at   | timestamp      |                                                                                      |
| updated_at   | timestamp      |                                                                                      |

### Category Type
The type of a category. Ex. Person, Expense type, etc

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| name       | text      |
| created_at | timestamp |
| updated_at | timestamp |

### Main Category
The category of a category. Ex. Shopping

| Name           | Type      |
|----------------|-----------|
| id             | uuid      |
| name           | text      |
| categoryTypeId | fk        |
| created_at     | timestamp |
| updated_at     | timestamp |

### Category
Transaction category. Ex. Shopping (hem)

| Name           | Type      |
|----------------|-----------|
| id             | uuid      |
| name           | text      |
| mainCategoryId | fk        |
| created_at     | timestamp |
| updated_at     | timestamp |

### Transaction - Category
Categorising a specific transaction

| Name          | Type      |
|---------------|-----------|
| transactionId | fk        |
| categoryId    | fk        |
| created_at    | timestamp |
| updated_at    | timestamp |

### External Source
Ex. SAS Eurobonus Masteracrd

| Name       | Type      |
|------------|-----------|
| id         | uuid      |
| name       | text      |
| created_at | timestamp |
| updated_at | timestamp |

### External Label
A label of a transaction from an external source. Ex. Mathem

| Name             | Type      |
|------------------|-----------|
| id               | uuid      |
| externalSourceId | fk        |
| name             | text      |
| created_at       | timestamp |
| updated_at       | timestamp |

### External Label - original transaction name
Connection of an original transaction name to an External label. For automatic processing of new data

| Name                    | Type      |
|-------------------------|-----------|
| externalLabelId         | fk        |
| originalTransactionName | text      |

### External label - Category
Categorising an external label

| Name            | Type      |
|-----------------|-----------|
| externalLabelId | fk        |
| categoryId      | fk        |
| created_at      | timestamp |
| updated_at      | timestamp |

### Recurring Transaction

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