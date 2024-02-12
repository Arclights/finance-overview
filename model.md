| Report |         |
|--------|---------|
| id     | uuid    |
| month  | text/id |
| year   | int     |

| Transaction |                |
|-------------|----------------|
| id          | uuid           |
| date        | date           |
| reportId    | fk             |
| type        | income/expense |
| amount      | decimal        |

| Category Type |      |
|---------------|------|
| id            | uuid |
| name          | text |

| Category       |      |
|----------------|------|
| id             | uuid |
| name           | text |
| categoryTypeId | fk   |

| Transaction - Category |    |
|------------------------|----|
| transactionId          | fk |
| categoryId             | fk |

| External Source |      |
|-----------------|------|
| id              | uuid |
| name            | text |

| External Labels  |      |
|------------------|------|
| id               | uuid |
| externalSourceId | fk   |
| name             | text |
| categoryId       | fk   |

