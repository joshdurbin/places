import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.durbs.places.PlacesModule
import io.durbs.places.chain.PlacesAddRemoveOperationsChain
import io.durbs.places.chain.PlacesQueryOperationsChain

import io.durbs.places.config.GlobalConfig
import io.durbs.places.config.MongoConfig
import io.durbs.places.config.RedisConfig
import io.durbs.places.service.PlaceService

import io.durbs.places.service.impl.MongoPlaceService
import io.durbs.places.service.impl.RedisPlaceService

import ratpack.config.ConfigData
import ratpack.rx.RxRatpack
import ratpack.server.Service
import ratpack.server.StartEvent

import static ratpack.groovy.Groovy.ratpack

ratpack {
  bindings {

    ConfigData configData = ConfigData.of { c ->
      c.yaml("$serverConfig.baseDir.file/application.yaml")
      c.env()
      c.sysProps()
    }

    bindInstance(MongoConfig, configData.get('/mongo', MongoConfig))
    bindInstance(RedisConfig, configData.get('/redis', RedisConfig))
    bindInstance(GlobalConfig, configData.get('/global', GlobalConfig))

    bindInstance(ObjectMapper, new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY))

    module PlacesModule

    bindInstance Service, new Service() {

      @Override
      void onStart(StartEvent event) throws Exception {

        RxRatpack.initialize()
      }
    }
  }

  handlers {

    prefix('insert') {
      all chain(registry.get(PlacesAddRemoveOperationsChain))
    }

    prefix('mongo') {
      all { next(single(PlaceService, get(MongoPlaceService))) }
      all chain(registry.get(PlacesQueryOperationsChain))
    }

    prefix('redis') {
      all { next(single(PlaceService, get(RedisPlaceService))) }
      all chain(registry.get(PlacesQueryOperationsChain))
    }

  }
}
