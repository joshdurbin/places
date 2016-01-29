package io.durbs.places

import com.google.inject.AbstractModule

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import io.durbs.places.chain.PlacesOperationsChain
import io.durbs.places.service.impl.ElasticsearchPlaceService
import io.durbs.places.service.impl.MongoPlaceService
import io.durbs.places.service.impl.RedisPlaceService
import io.durbs.places.service.impl.RethinkPlaceService

@CompileStatic
@Slf4j
class PlacesModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(MongoPlaceService)
    bind(RedisPlaceService)
    bind(RethinkPlaceService)
    bind(ElasticsearchPlaceService)
    bind(PlacesOperationsChain)
  }

}

