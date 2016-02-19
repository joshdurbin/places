package io.durbs.places.mongo

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.async.client.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.connection.ClusterSettings
import com.mongodb.connection.ConnectionPoolSettings
import com.mongodb.connection.ServerSettings
import com.mongodb.connection.SocketSettings
import com.mongodb.connection.SslSettings
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import ratpack.config.ConfigData
import ratpack.server.Service
import ratpack.server.StartEvent

@CompileStatic
@Slf4j
class MongoModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(MongoPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  MongoConfig provideConfig(final ConfigData configData) {

    configData.get(MongoConfig.CONFIG_ROOT, MongoConfig)
  }

  @Provides
  @Singleton
  MongoDatabase provide(final MongoConfig mongoConfig) {

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

  @Provides
  @Singleton
  public Service setup(final MongoConfig mongoConfig) {

    new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

        MongoClient mongoClient

        try {

          // OPEN A BLOCKING CONNECTION
          mongoClient = new MongoClient(new MongoClientURI(mongoConfig.uri))

          if (!mongoClient.listDatabaseNames().contains(mongoConfig.db)) {

            log.info("Creating Mongo DB '${mongoConfig.db}'")

            // GET IMPLICITLY CREATES THE DB
            mongoClient.getDatabase(mongoConfig.db)
          }

          if (!mongoClient.getDatabase(mongoConfig.db).listCollectionNames().contains(mongoConfig.collection)) {

            log.info("Creating Mongo collection '${mongoConfig.collection}' in DB '${mongoConfig.db}'")
            mongoClient.getDatabase(mongoConfig.db)
              .createCollection(mongoConfig.collection)
          }

          if (mongoClient.getDatabase('places').getCollection('places').listIndexes().findAll { Document index -> index.name == mongoConfig.indexName }.empty) {

            log.info("Creating Mongo index '${mongoConfig.indexName}' in collection '${mongoConfig.collection}' in DB '${mongoConfig.db}'")

            mongoClient.getDatabase(mongoConfig.db)
              .getCollection(mongoConfig.collection)
              .createIndex(
              new Document('loc', '2dsphere'), new IndexOptions(name: mongoConfig.indexName))
          }

        } finally {

          mongoClient?.close()
        }
      }
    }
  }

}

