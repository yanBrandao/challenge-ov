# one

This is a very interesting problem—a type of task unlike anything else I’ve done today—because it requires interpreting what the client wants to achieve and translating that into a chart.

Clearly, this kind of situation is part of our everyday routine; we use various products whose final output is a chart. Excel is probably the simplest example: we provide a table with information and then convert that into a chart based on certain parameters and settings.

For something more complex, we also have options like Grafana or ELK. These tools use their own language to translate textual information, which means the user must be familiar with both the data and the language in order to build the charts as desired.

The simplest approach to building this might be to expose the SQL language directly in a more user-friendly way—encapsulating the details necessary for executing SQL.

For example:

With a logged-in user, all requests will be based on the account. In this way, the user can choose between two initial tables.

```sql
Available Tables [GEOFENCE, ASSET_ACCOUNT_HISTORY]

LIST FIELDS
...
FROM 
...
WHERE
...
```

--------
Then the user chooses the GEOFENCE table:
```sql
LIST FIELDS
...
FROM 
{GEOFENCE}
WHERE
...
```
--------

Upon choosing the GEOFENCE table, more options appear:

```sql
Available Fields [name, description, polygon, maxHoursInFence, useForTrip]

Available Tables [ASSET_ACCOUNT_HISTORY, GEOFENCE_TYPE, DEMURRAGE]
```

And then the user chooses GEOFENCE_TYPE:

```sql
LIST FIELDS 
...
FROM 
{GEOFENCE} {GEOFENCE_TYPE}
WHERE
...
```
And so on.

Finally, following the examples presented in the problem, we would get something like:
```sql
LIST FIELDS
{COUNT(ASSET.id)}
FROM
{GEOFENCE}{GEOFENCE_TYPE}{ASSET}{PRODUCT}
WHERE
{PRODUCT.isGas = true}
```

On the backend, these inputs would be transformed into something similar to the JSON examples below:

- Case 1: Assets in Production Geofences with Gas
```json
{
    "dataSources": ["ASSET", "DEMURRAGE", "GEOFENCE", "GEOFENCE_TYPE"],
    "field": [
        { "field": "ASSET.number", "alias": "asset_number" },
        { "field": "PRODUCT.isGas", "alias": "is_gas" }
    ],
    "filters": [
        { "field": "GEOFENCE_TYPE.name", "operator": "=", "value": "Production" },
        { "field": "DEMURRAGE.leave_time", "operator": "IS", "value": "NULL" }
    ],
    "visualization": "TABLE"
}

- Case 2: Date histogram showing how many assets were on a specific Production site in the last month
```json
{
  "dataSources": ["DEMURRAGE", "GEOFENCE", "GEOFENCE_TYPE"],
  "fields": [
    { "field": "enter_time", "transform": "DATE_TRUNC('day')", "alias": "day" },
    { "field": "asset_id", "aggregation": "COUNT_DISTINCT", "alias": "asset_count" }
  ],
  "filters": [
    { "field": "GEOFENCE_TYPE.name", "operator": "=", "value": "Production" },
    { "field": "enter_time", "operator": ">=", "value": "LAST_MONTH()" }
  ],
  "groupBy": ["day"],
  "visualization": "LINE_CHART"
}
```

For this, we will need a Query Builder, and the corresponding classes would be:

```java
data class QueryRequest(
    val dataSources: List<String>,
    val fields: List<FieldConfig>,
    val filters: List<FilterConfig>,
    val groupBy: List<String>?,
    val visualization: String
)

data class FieldConfig(
    val field: String,
    val transform: String?,
    val aggregation: String?,
    val alias: String
)

data class FilterConfig(
    val field: String,
    val operator: String,
    val value: String
)

data class TableMetadata(
    val tableName: String,
    val joins: List<JoinConfig>
)

data class JoinConfig(
    val targetTable: String,
    val condition: String
)
```