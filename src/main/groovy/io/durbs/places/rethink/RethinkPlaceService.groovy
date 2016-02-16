package io.durbs.places.rethink

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.GetIntersecting
import com.rethinkdb.gen.ast.GetNearest
import com.rethinkdb.net.Connection
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.Place
import io.durbs.places.PlaceService
import ratpack.exec.Blocking
import rx.Observable
import rx.functions.Func1

@Singleton
@CompileStatic
@Slf4j
class RethinkPlaceService implements PlaceService {

  @Inject
  Connection.Builder connectionBuilder

  @Inject
  RethinkDB rethinkDB

  @Override
  Observable<Integer> insertPlace(final Place place) {

    Blocking.get {

      final Connection connection
      final Map<String, String> operationResult

      try {

        connection = connectionBuilder.connect()
        operationResult = RethinkConversionFunctions.CREATE_INSERT_COMMAND_FOR_PLACE(rethinkDB, place).run(connection) as Map

      } finally {

        connection?.close()
      }

      operationResult.get('inserted') as Integer

    }.observe()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    final GetIntersecting getIntersectingCommand = rethinkDB.table('places')
      .getIntersecting(
        rethinkDB.circle(rethinkDB.array(longitude, latitude), searchRadius)
      ).optArg('index', 'location')

    Blocking.get {

      final Connection connection
      final List result

      try {

        connection = connectionBuilder.connect()
        result = getIntersectingCommand.run(connection) as List

      } finally {

        connection?.close()
      }

      result
    }
    .observeEach()
    .map(RethinkConversionFunctions.MAP_DOCUMENT_TO_PLACE)
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {

    getPlaces(latitude, longitude, searchRadius)
      .map({Place place ->

      new GeoWithin<Place>(place, 0.0 as Double, 0L, new GeoCoordinates(place.latitude, place.longitude))
    } as Func1)
      .bindExec()
  }
}
