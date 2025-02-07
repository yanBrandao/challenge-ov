# two

First, I had to work on the file.

I used sublime_text to convert the SQL into CSV, allowing me to use a more performant operation in Postgres with the COPY FROM file command.

Then, I created a docker-compose file to start Postgres and create the table using the startup.sql file.

Next, I executed the following command:
docker exec -it ovinto_postgres bash

After that, I ran the following sequence of commands to import the file:

```bash

psql -U postgres

\c test

COPY standstills FROM '/home/standstills.csv' WITH (FORMAT CSV, HEADER, NULL 'NULL');

```

The final result will be:


`COPY 2493700`

Now that everything is configured, we can start the exercise 😂.

## Part A

First, we need to find the assets that were present in the geofence in the year 2020. For this, we use the following SELECT:

```sql
SELECT DISTINCT
             f_asset,
             entered_timestamp AS present_when
         FROM standstills
         WHERE entered_timestamp >= '2020-01-01'
           AND entered_timestamp < '2021-01-01'
         and f_geofence = '9d44db9f-e4a1-11e6-93da-d8cb8a7fb17b'
```

We use the DISTINCT clause to ensure that assets that entered the geofence more than once on the same day are only counted once.

After that, we use this result as a sub-select to count the assets by day of the year:

```sql
SELECT
    present_when,
    COUNT(f_asset) AS asset_count
FROM (
         SELECT DISTINCT
             f_asset,
             entered_timestamp AS present_when
         FROM standstills
         WHERE entered_timestamp >= '2020-01-01'
           AND entered_timestamp < '2021-01-01'
         and f_geofence = '9d44db9f-e4a1-11e6-93da-d8cb8a7fb17b'
     ) AS assetsInGeofence
GROUP BY present_when
ORDER BY present_when;
```

One way to optimize this SELECT is by creating a stored procedure with a parameter for f_geofence, so that we can run it for different geofences.

## Part B

Using the previous query, the main change is to add the difference between the day an asset entered and the day it left the location.

The final result is the following query:

```sql
SELECT
    present_when,
    COUNT(f_asset) AS asset_count,
    assets_stood_longer_than_5_days
FROM (
         SELECT DISTINCT
             st.f_asset,
             st.entered_timestamp AS present_when,
             count(CASE when st.left_timestamp - st.entered_timestamp > 5 THEN 1 END) as assets_stood_longer_than_5_days
         FROM standstills st
         WHERE entered_timestamp >= '2020-01-01'
           AND entered_timestamp < '2021-01-01'
           and f_geofence = '9d44db9f-e4a1-11e6-93da-d8cb8a7fb17b'
         group by st.entered_timestamp, st.f_asset
     ) AS assetsInGeofence
GROUP BY present_when, assets_stood_longer_than_5_days
ORDER BY present_when;
```

## Part C

We can create composite indexes.

For example, create a composite index on the columns that appear in the WHERE clause (i.e., on `(f_geofence, entered_timestamp)`):

```sql
CREATE INDEX idx_standstills_geofence_date
    ON standstills (f_geofence, entered_timestamp)
    WHERE entered_timestamp >= '2020-01-01'
      AND entered_timestamp < '2021-01-01';
```

We can also partition the table by month so that we can process the data in chunks and generate a monthly report instead of an annual one:

```sql
CREATE TABLE standstills_2020_01 PARTITION OF standstills
FOR VALUES FROM ('2020-01-01') TO ('2020-02-01');
```