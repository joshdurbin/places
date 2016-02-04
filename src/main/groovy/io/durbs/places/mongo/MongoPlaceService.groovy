package io.durbs.places.mongo

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.GlobalConfig
import io.durbs.places.Place
import io.durbs.places.PlaceService
import org.bson.Document
import rx.Observable
import rx.functions.Func1

@Singleton
@CompileStatic
@Slf4j
class MongoPlaceService implements PlaceService {

  @Inject
  MongoDatabase mongoDatabase

  @Inject
  MongoConfig mongoConfig

  @Inject
  GlobalConfig globalConfig

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

}
