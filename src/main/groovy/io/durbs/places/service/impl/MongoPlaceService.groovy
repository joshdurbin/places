package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
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
import io.durbs.places.codec.MongoPlaceCodec
import io.durbs.places.config.GlobalConfig
import io.durbs.places.config.MongoConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import rx.Observable
import rx.functions.Func1

@Singleton
@CompileStatic
@Slf4j
class MongoPlaceService implements PlaceService {

  final MongoDatabase mongoDatabase
  final MongoConfig mongoConfig
  final GlobalConfig globalConfig

  @Inject
  MongoPlaceService(MongoConfig mongoConfig, GlobalConfig globalConfig) {

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

    this.mongoConfig = mongoConfig
    this.globalConfig = globalConfig

    mongoDatabase = MongoClients.create(mongoClientSettings).getDatabase(mongoConfig.db)
  }

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
      new Document('$near',
        new Document('$geometry',
          new Document('type', 'Point')
            .append('coordinates', [longitude, latitude])
        )
          .append('$maxDistance', searchRadius)
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
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {

    final Document bsonCommand = new Document('geoNear', mongoConfig.collection)
      .append('spherical', true)
      .append('limit', globalConfig.resultSetSize)
      .append('maxDistance', searchRadius)
      .append('near', new Document('type', 'point').append('coordinates', [ longitude, latitude ]))
    
    mongoDatabase.runCommand(bsonCommand)
      .flatMap({ Document document ->

      Observable.from(document.get('results'))
    } as Func1)
      .map({ Document document ->

      new GeoWithin<Place>(new Place(), 0.0 as Double, 0L, new GeoCoordinates(123, 456))
    } as Func1)
    .bindExec()
  }

}
