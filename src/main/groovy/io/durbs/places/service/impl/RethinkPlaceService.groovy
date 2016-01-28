package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.GetNearest
import com.rethinkdb.net.Connection
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.codec.RethinkConversionFunctions
import io.durbs.places.config.RethinkConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
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
  RethinkConfig rethinkConfig

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

    final GetNearest getNearestCommand = rethinkDB.table('places')
      .getNearest(rethinkDB.point(longitude, latitude))
      .optArg('index', 'location')
      .optArg('max_dist', searchRadius)

    Blocking.get {

      final Connection connection
      final List result

      try {

        connection = connectionBuilder.connect()
        result = getNearestCommand.run(connection) as List

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
  void prepareDatastore() {

    final Connection connection

    try {

      connection = connectionBuilder.connect()

      if (!(rethinkDB.dbList().run(connection) as List<String>).contains(rethinkConfig.db)) {

        log.info("Creating Rethink DB '${rethinkConfig.db}'")
        rethinkDB.dbCreate(rethinkConfig.db).run(connection)
      }

      if (!(rethinkDB.db(rethinkConfig.db).tableList().run(connection) as List<String>).contains(rethinkConfig.table)) {

        log.info("Creating Rethink table '${rethinkConfig.table}' in DB '${rethinkConfig.db}'")
        rethinkDB.db(rethinkConfig.db).tableCreate(rethinkConfig.table).run(connection)
      }

      if (!(rethinkDB.db(rethinkConfig.db).table(rethinkConfig.table).indexList().run(connection) as List<String>).contains('location')) {

        log.info("Creating Rethink index 'location' in table '${rethinkConfig.table}' in DB '${rethinkConfig.db}'")
        rethinkDB.db(rethinkConfig.db).table(rethinkConfig.table).indexCreate('location').optArg('geo', true).run(connection)
      }

    } finally {

      connection?.close()
    }
  }
}
