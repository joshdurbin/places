Places -- Geo Near Performance Tests
-----------------------------

This project intends to leverage the recommended implementation path for geospatial, "geo near" queries against a data set of
places of interest (POIs) backed by various datastores. The test data set contains POIs only for the San Francisco Bay Area.
The Ratpack and RxJava powered API allows for the insertion and query of POIs.

POST operations to the '/places' end point insert a single record into the backing datastore (see below for an example record).

GET operations to '/places' w/ the point and radius information will query the backing datastore. The expected structure of the query
  endpoint is '/places/$latitude/$longitude/$radius' w/ a radius in meters.

The available backing datastores are:

1. Mongo
2. Redis (3.2 beta)
3. Elasticsearch
4. RethinkDB
5. PostgreSQL w/postgis (coming, see [add-dynamo_postgis_couchbase](https://github.com/joshdurbin/places/tree/add-dynamo_postgis_couchbase) branch)
6. AWS DynamoDB (coming, see [add-dynamo_postgis_couchbase](https://github.com/joshdurbin/places/tree/add-dynamo_postgis_couchbase) branch)
7. Couchase (coming, see [add-dynamo_postgis_couchbase](https://github.com/joshdurbin/places/tree/add-dynamo_postgis_couchbase) branch)

-----------------------------

Testing:

All the aforementioned datastores are implemented and thus their dependencies are delivered, required for compilation. However, only one datastore service
stack is loaded when the application loads. This is controlled by the [global:datastoreTarget](https://github.com/joshdurbin/places/blob/master/src/ratpack/application.yaml) attribute in the shipped base
config. Valid options for this config are in the `Datastore` enum found in the [GlobalConfig.groovy](https://github.com/joshdurbin/places/blob/master/src/main/groovy/io/durbs/places/GlobalConfig.groovy) class.

-----------------------------

Sample record:

```json
{
  "name": "Fitness SF Fillmore",
  "address": "1455 Fillmore St",
  "city": "San Francisco",
  "state": "CA",
  "zipCode": "94115",
  "telephoneNumber": "(415) 927-4653",
  "neighborhoods": [
    "Thomas Paine Square",
    "Japantown",
    "Western Addition"
  ],
  "categories": [
    "Sports and Recreation",
    "Gyms and Fitness Centers"
  ],
  "latitude": 37.782874,
  "longitude": -122.432868
}
```
