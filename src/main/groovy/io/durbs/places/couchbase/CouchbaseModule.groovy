package io.durbs.places.couchbase

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import ratpack.config.ConfigData

@CompileStatic
@Slf4j
class CouchbaseModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(CouchbasePlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  CouchbaseConfig provideConfig(final ConfigData configData) {

    configData.get(CouchbaseConfig.CONFIG_ROOT, CouchbaseConfig)
  }

}

