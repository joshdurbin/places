package io.durbs.places.rethink

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.Count
import com.rethinkdb.gen.ast.Insert
import com.rethinkdb.gen.ast.Limit
import com.rethinkdb.net.Connection
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.GlobalConfig
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

  @Inject
  GlobalConfig globalConfig

  @Inject
  RethinkConfig rethinkConfig

  @Override
  Observable<Integer> insertPlace(final Place place) {

    // CONSTRUCT COMMAND OUTSIDE BLOCKING THREAD, MINIMIZE BLOCKING THREAD TO IO USAGE
    final Insert insertCommand = RethinkConversionFunctions.CREATE_INSERT_COMMAND_FOR_PLACE(rethinkDB, rethinkConfig.table, place)

    Blocking.get {

      final Connection connection
      final Map<String, String> operationResult

      try {

        connection = connectionBuilder.connect()
        operationResult = insertCommand.run(connection) as Map

      } finally {

        connection?.close()
      }

      operationResult.get('inserted') as Integer

    }.observe()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    // CONSTRUCT COMMAND OUTSIDE BLOCKING THREAD, MINIMIZE BLOCKING THREAD TO IO USAGE
    final Limit getIntersectingCommand = rethinkDB.table(rethinkConfig.table)
      .getIntersecting(
        rethinkDB.circle(rethinkDB.array(longitude, latitude), searchRadius)
          .optArg('num_vertices', rethinkConfig.numOfVertices))
      .optArg('index', rethinkConfig.indexKey)
      .limit(globalConfig.resultSetSize as Integer)

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

  @Override
  Observable<Integer> getNumberOfPlaces() {

    final Count countCommand = rethinkDB.table(rethinkConfig.table).count()

    Blocking.get {

      final Connection connection
      final Integer count

      try {

        connection = connectionBuilder.connect()
        count = countCommand.run(connection) as Integer

      } finally {

        connection?.close()
      }

      count

    }.observe()
  }
}
