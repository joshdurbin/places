package io.durbs.places.dynamo

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
class DynamoModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(DynamoPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  DynamoConfig provideConfig(final ConfigData configData) {

    configData.get(DynamoConfig.CONFIG_ROOT, DynamoConfig)
  }

}

