package io.durbs.places

import com.google.inject.Inject
import com.google.inject.Singleton
import groovy.util.logging.Slf4j
import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson
import rx.functions.Func1

import static ratpack.jackson.Jackson.fromJson

@Singleton
@Slf4j
class RESTChain extends GroovyChainAction {

  @Inject
  GlobalConfig globalConfig

  @Inject
  PlaceService placeService

  @Override
  void execute() throws Exception {

    post {

      parse(fromJson(Place))
        .observe()
        .flatMap ({ final Place place ->

        placeService.insertPlace(place)

      } as Func1)
        .single()
        .subscribe { final Integer numberOfInserts ->

        render Jackson.json(numberOfInserts)
      }
    }

    get('count') {

      placeService.getNumberOfPlaces()
        .subscribe { final Integer numberOfPlaces ->

        render Jackson.json(numberOfPlaces)
      }
    }

    get(':latitude/:longitude/:radius') {

      final Double latitude = pathTokens['latitude'] as Double
      final Double longitude = pathTokens['longitude'] as Double
      final Double radius = Double.valueOf(pathTokens.get('radius', globalConfig.defaultSearchRadius as String))

      final Double queryRadius

      if (radius &&  radius <= globalConfig.maxAllowableSearchRadius) {
        queryRadius = radius
      } else {
        queryRadius = globalConfig.defaultSearchRadius
      }

      final Integer resultSize
      if (request.queryParams.containsKey('limit')
        && ((request.queryParams.get('limit') as Integer) > globalConfig.maxResultSize)) {

        resultSize = globalConfig.defaultResultSize

      } else if (request.queryParams.containsKey('limit')
        && (request.queryParams.get('limit') as Integer) <= globalConfig.maxResultSize) {

        resultSize = request.queryParams.get('limit') as Integer

      } else {
        resultSize = globalConfig.defaultResultSize
      }

      final Boolean withDistance = request.queryParams.containsKey('distance')

      if (withDistance) {

        placeService.getPlacesWithDistance(latitude, longitude, queryRadius, resultSize)
          .toList()
          .subscribe { List<PlaceWithDistance> places ->

          render Jackson.json(places)
        }

      } else {

        placeService.getPlaces(latitude, longitude, queryRadius, resultSize)
          .toList()
          .subscribe { List<Place> places ->

          render Jackson.json(places)
        }
      }
    }
  }
}
