package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoArgs
import com.lambdaworks.redis.GeoWithin
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.lambdaworks.redis.codec.CompressionCodec
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.codec.RedisPlaceCodec
import io.durbs.places.config.GlobalConfig
import io.durbs.places.config.RedisConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import rx.Observable
import rx.functions.Func1

@CompileStatic
@Singleton
@Slf4j
class RedisPlaceService implements PlaceService {

  final RedisConfig redisConfig
  final GlobalConfig globalConfig
  final RedisReactiveCommands<String, Place> redisPlaceCommands

  @Inject
  RedisPlaceService(RedisConfig redisConfig, GlobalConfig globalConfig) {

    this.redisConfig = redisConfig
    this.globalConfig = globalConfig

    redisPlaceCommands = RedisClient.create(redisConfig.uri).connect(
      CompressionCodec.valueCompressor(
        new RedisPlaceCodec(),
        CompressionCodec.CompressionType.GZIP)
    ).reactive()
  }

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

}
