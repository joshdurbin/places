package io.durbs.places.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import io.durbs.places.config.GlobalConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson
import rx.functions.Func1

import static ratpack.jackson.Jackson.fromJson

@Singleton
class PlacesQueryOperationsChain extends GroovyChainAction {

  @Inject
  GlobalConfig globalConfig

  @Override
  void execute() throws Exception {

    post { PlaceService placeService ->

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

    get(':latitude/:longitude/:radius') { PlaceService placeService ->

      final Double latitude = pathTokens['latitude'] as Double
      final Double longitude = pathTokens['longitude'] as Double
      final Double radius = Double.valueOf(pathTokens.get('radius', globalConfig.defaultSearchRadius as String))

      final Double queryRadius

      if (radius &&  radius <= globalConfig.maxAllowableSearchRadius) {
        queryRadius = radius
      } else {
        queryRadius = globalConfig.defaultSearchRadius
      }

      final Boolean withDistance = request.queryParams.containsKey('distance')

      if (withDistance) {

        placeService.getPlacesWithDistance(latitude, longitude, queryRadius)
          .toList()
          .subscribe { List<GeoWithin<Place>> places ->

          render Jackson.json(places)
        }

      } else {

        placeService.getPlaces(latitude, longitude, queryRadius)
          .toList()
          .subscribe { List<Place> places ->

          render Jackson.json(places)
        }
      }
    }
  }
}
