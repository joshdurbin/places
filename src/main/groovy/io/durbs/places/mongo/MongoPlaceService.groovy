package io.durbs.places.mongo

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.QueryOperators
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.Place
import io.durbs.places.PlaceService
import io.durbs.places.PlaceWithDistance
import org.bson.Document
import rx.Observable
import rx.functions.Func1

@Singleton
//@CompileStatic
@Slf4j
class MongoPlaceService implements PlaceService {

  @Inject
  MongoDatabase mongoDatabase

  @Inject
  MongoConfig mongoConfig

  @Override
  Observable<Integer> insertPlace(final Place place) {

    mongoDatabase.getCollection(mongoConfig.collection, Place)
      .insertOne(place)
      .count()
      .bindExec()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius, final Integer maxResultSize) {

    final Document bsonFilter = new Document('loc',
      new Document(QueryOperators.NEAR,
        new Document('$geometry',
          new Document('type', 'Point')
            .append('coordinates', [longitude, latitude])
        )
          .append(QueryOperators.MAX_DISTANCE, searchRadius)
          .append('$limit', maxResultSize)
        )
    )

    mongoDatabase.getCollection(mongoConfig.collection, Place)
      .find(bsonFilter)
      .limit(maxResultSize)
      .toObservable()
      .bindExec()
  }


  @Override
  Observable<PlaceWithDistance> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius, final Integer maxResultSize) {

    final Document bsonCommand = new Document('geoNear', mongoConfig.collection)
      .append('spherical', true)
      .append('limit', maxResultSize)
      .append('maxDistance', searchRadius)
      .append('near', new Document('type', 'point').append('coordinates', [ longitude, latitude ]))

    mongoDatabase.runCommand(bsonCommand)
      .flatMap({ Document document ->

      Observable.from(document.get('results') as List)
    } as Func1)
      .map({ final Document document ->

      final Document hitObject = document.get('obj', Document);

      new PlaceWithDistance(id: hitObject.get('_id') as String,
        name: hitObject.get('name', String),
        address: hitObject.get('address', String),
        city: hitObject.get('city', String),
        state: hitObject.get('state', String),
        zipCode: hitObject.get('zipCode', String),
        telephoneNumber: hitObject.get('telephoneNumber', String),
        categories: hitObject.get('categories', List),
        neighborhoods: hitObject.get('neighborhoods', List),
        latitude: hitObject?.get('loc', Document)?.get('coordinates', List)?.last(),
        longitude: hitObject?.get('loc', Document)?.get('coordinates', List)?.first(),
        distance: document.get('dis', Double))
    } as Func1)
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
