package io.durbs.places.rethink

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import ratpack.config.ConfigData
import ratpack.server.Service
import ratpack.server.StartEvent

@CompileStatic
@Slf4j
class RethinkModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(RethinkPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  RethinkConfig provideConfig(final ConfigData configData) {

    configData.get(RethinkConfig.CONFIG_ROOT, RethinkConfig)
  }

  @Provides
  @Singleton
  RethinkDB provideRethinkDBSingleton() {
    RethinkDB.r
  }

  @Provides
  @Singleton
  Connection.Builder provideRethinkConnectionBuilder(final RethinkConfig rethinkConfig) {

    Connection.build()
      .hostname(rethinkConfig.hostname)
      .port(rethinkConfig.port)
      .db(rethinkConfig.db)
  }

  @Provides
  @Singleton
  public Service setup(final RethinkConfig rethinkConfig, final RethinkDB rethinkDB, final Connection.Builder connectionBuilder) {

    new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

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
  }

}

