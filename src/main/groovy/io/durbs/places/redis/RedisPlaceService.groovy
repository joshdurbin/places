package io.durbs.places.redis

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoArgs
import com.lambdaworks.redis.GeoWithin
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.GlobalConfig
import io.durbs.places.Place
import io.durbs.places.PlaceService
import rx.Observable
import rx.functions.Func1

@CompileStatic
@Singleton
@Slf4j
class RedisPlaceService implements PlaceService {

  @Inject
  RedisConfig redisConfig

  @Inject
  GlobalConfig globalConfig

  @Inject
  RedisReactiveCommands<String, Place> redisPlaceCommands

  @Override
  Observable<Integer> insertPlace(final Place place) {

    redisPlaceCommands.geoadd(redisConfig.geoSetKey, place.longitude, place.latitude, place)
      .map({ final Long insertionCount ->

      insertionCount.intValue()
    } as Func1<Long, Integer>).bindExec()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    redisPlaceCommands.georadius(redisConfig.geoSetKey,
      longitude,
      latitude,
      searchRadius,
      GeoArgs.Unit.m,
      new GeoArgs().withCount(globalConfig.resultSetSize)
    ).map({ GeoWithin<Place> input ->

      input.member
    } as Func1)
      .bindExec()
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {

    redisPlaceCommands.georadius(redisConfig.geoSetKey,
      longitude,
      latitude,
      searchRadius,
      GeoArgs.Unit.m,
      new GeoArgs()
        .withCount(globalConfig.resultSetSize)
        .withCoordinates()
        .withDistance()
    ).bindExec()
  }

  @Override
  Observable<Integer> getNumberOfPlaces() {

    redisPlaceCommands.zcard(redisConfig.geoSetKey)
      .map( { final Long cardinality ->

      cardinality as Integer
    } as Func1)
    .bindExec()
  }
}
