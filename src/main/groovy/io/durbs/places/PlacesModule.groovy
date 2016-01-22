package io.durbs.places

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.chain.PlacesAddRemoveOperationsChain
import io.durbs.places.chain.PlacesQueryOperationsChain
import io.durbs.places.codec.MongoPlaceCodec
import io.durbs.places.codec.RedisPlaceCodec
import io.durbs.places.config.MongoConfig
import io.durbs.places.config.RedisConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.impl.MongoPlaceService
import io.durbs.places.service.impl.RedisPlaceService
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

@CompileStatic
@Slf4j
class PlacesModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(MongoPlaceService)
    bind(RedisPlaceService)
    bind(PlacesAddRemoveOperationsChain)
    bind(PlacesQueryOperationsChain)
  }

  @Provides
  @Singleton
  RedisReactiveCommands<String, Place> provideRedisClient(RedisConfig redisConfig) {

    RedisClient.create(redisConfig.uri).connect(new RedisPlaceCodec()).reactive()
  }

  @Provides
  @Singleton
  MongoDatabase provideMongoDB(MongoConfig mongoConfig) {

    final ConnectionString connectionString = new ConnectionString(mongoConfig.uri)

    final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
      MongoClient.getDefaultCodecRegistry(),
      CodecRegistries.fromCodecs(new MongoPlaceCodec()))

    final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
      .codecRegistry(codecRegistry)
      .clusterSettings(ClusterSettings.builder().applyConnectionString(connectionString).build())
      .connectionPoolSettings(ConnectionPoolSettings.builder().applyConnectionString(connectionString).build())
      .serverSettings(ServerSettings.builder().build()).credentialList(connectionString.getCredentialList())
      .sslSettings(SslSettings.builder().applyConnectionString(connectionString).build())
      .socketSettings(SocketSettings.builder().applyConnectionString(connectionString).build())
      .build()

    MongoClients.create(mongoClientSettings).getDatabase(mongoConfig.db)
  }
}

