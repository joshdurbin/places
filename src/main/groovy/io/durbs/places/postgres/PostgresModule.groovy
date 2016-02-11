package io.durbs.places.postgres

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.zaxxer.hikari.HikariConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import ratpack.config.ConfigData
import ratpack.hikari.HikariModule

import javax.sql.DataSource

@CompileStatic
@Slf4j
class PostgresModule extends AbstractModule {

  @Override
  protected void configure() {

    install(new HikariModule())
    bind(PlaceService).to(PostgresPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  HikariConfig provideConfig(final ConfigData configData) {

    configData.get('/postgres', HikariConfig)
  }

  @Provides
  @Singleton
  public DSLContext dslContext(final DataSource dataSource) {
    return DSL.using(new DefaultConfiguration().derive(dataSource));
  }

}

