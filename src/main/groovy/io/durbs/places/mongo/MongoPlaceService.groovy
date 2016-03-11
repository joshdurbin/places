package io.durbs.places.mongo

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.QueryOperators
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
      new Document(QueryOperators.NEAR,
        new Document('$geometry',
          new Document('type', 'Point')
            .append('coordinates', [longitude, latitude])
        )
          .append(QueryOperators.MAX_DISTANCE, searchRadius)
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
  Observable<Integer> getNumberOfPlaces() {

    mongoDatabase.getCollection(mongoConfig.collection).count()
    .map({ final Long count ->
      count as Integer
    } as Func1)
    .bindExec()
  }
}
