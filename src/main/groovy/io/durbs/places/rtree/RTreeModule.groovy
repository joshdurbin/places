package io.durbs.places.rtree

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
class RTreeModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(RTreePlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  RTreeConfig provideConfig(final ConfigData configData) {

    configData.get(RTreeConfig.CONFIG_ROOT, RTreeConfig)
  }

}

