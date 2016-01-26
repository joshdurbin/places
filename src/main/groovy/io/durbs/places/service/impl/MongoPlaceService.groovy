package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import com.mongodb.client.model.IndexOptions
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.config.GlobalConfig
import io.durbs.places.config.MongoConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import org.bson.Document
import rx.Observable
import rx.functions.Func1

@CompileStatic
@Singleton
@Slf4j
class MongoPlaceService implements PlaceService {

  @Inject
  GlobalConfig globalConfig

  @Inject
  MongoConfig mongoConfig

  @Inject
  MongoDatabase mongoDatabase

  @Override
  Observable<Integer> insertPlace(final Place place) {

    mongoDatabase.getCollection(mongoConfig.collection, Place)
      .insertOne(place)
      .count()
      .bindExec()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    final Document bsonFilter = new Document('loc',
      new Document('$near',
        new Document('$geometry',
          new Document('type', 'Point')
            .append('coordinates', [longitude, latitude])
        )
          .append('$maxDistance', searchRadius)
          .append('$limit', globalConfig.resultSetSize)
        )
    )

    mongoDatabase.getCollection(mongoConfig.collection, Place)
      .find(bsonFilter)
      .limit(globalConfig.resultSetSize as Integer)
      .toObservable()
      .bindExec()
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {

    final Document bsonCommand = new Document('geoNear', mongoConfig.collection)
      .append('spherical', true)
      .append('limit', globalConfig.resultSetSize)
      .append('maxDistance', searchRadius)
      .append('near', new Document('type', 'point').append('coordinates', [ longitude, latitude ]))
    
    mongoDatabase.runCommand(bsonCommand)
      .flatMap({ Document document ->

      Observable.from(document.get('results'))
    } as Func1)
      .map({ Document document ->

      new GeoWithin<Place>(new Place(), 0.0 as Double, 0L, new GeoCoordinates(123, 456))
    } as Func1)
    .bindExec()
  }

  @Override
  void prepareDatastore() {

    mongoDatabase.getCollection(mongoConfig.collection)
      .createIndex(null, new IndexOptions())
  }
}
