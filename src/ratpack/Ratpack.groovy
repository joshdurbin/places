import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.durbs.places.RESTChain
import io.durbs.places.GlobalConfig
import io.durbs.places.mongo.MongoModule
import ratpack.config.ConfigData
import ratpack.rx.RxRatpack

import static ratpack.groovy.Groovy.ratpack

ratpack {

  bindings {

    RxRatpack.initialize()

    // READ CONFIG DATA AND PLACE IN GUICE
    final ConfigData configData = ConfigData.of { c ->
      c.yaml("$serverConfig.baseDir.file/application.yaml")
      c.env()
      c.sysProps()
    }
    bindInstance(ConfigData, configData)
    
    // LOCAL GLOBAL CONFIG USED FOR MODULE LOADING BELOW
    final GlobalConfig globalConfig = configData.get(GlobalConfig.CONFIG_ROOT, GlobalConfig)
    bindInstance(GlobalConfig, globalConfig)
    module MongoModule

    // BIND JACKSON OBJECT MAPPER THAT IGNORES NULL AND EMPTY VALUES
    bindInstance(ObjectMapper, new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY))
  }

  handlers {

    // PASS ALL HTTP TRAFFIC TO THE OPERATIONS CHAIN
    prefix('places') {
      all chain(registry.get(RESTChain))
    }
  }
}
