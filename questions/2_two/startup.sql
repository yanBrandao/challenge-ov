CREATE DATABASE test;

\c test;

CREATE TABLE standstills (
    id varchar(100),
    createdat date,
    deleted boolean,
    entered_timestamp date,
    left_timestamp date,
    f_asset varchar(100),
    f_geofence varchar(100)
)