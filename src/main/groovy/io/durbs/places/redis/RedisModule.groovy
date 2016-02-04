package io.durbs.places.redis

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.lambdaworks.redis.codec.CompressionCodec
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.Place
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import ratpack.config.ConfigData
import ratpack.server.Service
import ratpack.server.StopEvent

@CompileStatic
@Slf4j
class RedisModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(RedisPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  RedisConfig provideConfig(final ConfigData configData) {

    configData.get(RedisConfig.CONFIG_ROOT, RedisConfig)
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, Place> provideReactiveCompressedPlaceRedisCommands(final RedisConfig redisConfig) {

    RedisClient.create(redisConfig.uri).connect(
      CompressionCodec.valueCompressor(
        new RedisPlaceCodec(),
        CompressionCodec.CompressionType.GZIP)
    ).reactive()
  }

  @Provides
  @Singleton
  public Service teardown(final RedisReactiveCommands<String, Place> redisCommands) {

    new Service() {

      @Override
      public void onStop(final StopEvent event) throws Exception {

        log.info('Closing redis commands...')
        redisCommands.close()

        log.debug('Redis commands closed.')
      }
    }
  }

}

