Places -- Geo Near Mongo Performance Tests
-----------------------------

This project intends to leverage the recommended implementation path for geospatial, "geo near" queries against a data set of
places of interest (POIs) backed by the Mongo data store. The test data set contains POIs only for the San Francisco Bay Area.
The Ratpack and RxJava powered API allows for the insertion and query of POIs.

POST operations to the `/places` end point insert a single record into the backing data store (see below for an example record).

GET operations to `/places` w/ the point and radius information will query the backing data store. The expected structure of the query
  endpoint is `/places/$latitude/$longitude/$radius` w/ a radius in meters.

-----------------------------

Requirements:

1. Install the latest version of Java 8 JDK
2. Install the latest version of [Groovy](http://www.groovy-lang.org/)
3. Install the latest version of [Gradle](http://gradle.org/)

Note: Unless your OS has up-to-date packages for Groovy, Gradle, etc... I recommend installing Java 8 and leveraging [sdkman](http://sdkman.io/)
for the installation of the other JVM-related tools.

-----------------------------

Data:

The data includes the name, address details, and relevant category information for each entry. There are a little more
than 650,000 entries in the supplied, test data set. The data set and conversion scripts can be found in the [data](https://github.com/joshdurbin/places/tree/data)
branch. The data set itself is tar.gz'ed

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

-----------------------------

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy?template=https://github.com/joshdurbin/places/tree/mongo_heroku_deployment)