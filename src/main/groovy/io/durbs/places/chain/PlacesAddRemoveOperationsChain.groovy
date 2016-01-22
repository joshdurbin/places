package io.durbs.places.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import io.durbs.places.domain.Place
import io.durbs.places.service.impl.MongoPlaceService
import io.durbs.places.service.impl.RedisPlaceService
import ratpack.groovy.handling.GroovyChainAction
import rx.Observable
import rx.functions.Func1

import static ratpack.jackson.Jackson.fromJson

@Singleton
class PlacesAddRemoveOperationsChain extends GroovyChainAction {

  @Inject
  MongoPlaceService mongoPlaceService

  @Inject
  RedisPlaceService redisPlaceService

  @Override
  void execute() throws Exception {

    post {

      parse(fromJson(Place))
        .observe()
        .flatMap ({ final Place place ->

        // verify that name and lat, long are supplied, if not, error

        Observable.zip(
          mongoPlaceService.insertPlace(place).single(),
          redisPlaceService.insertPlace(place).single())
          { Integer mongoInsertionCount, Integer redisInsertionCount ->
            "Mongo: ${mongoInsertionCount}, Redis: ${redisInsertionCount}" as String
          }

      } as Func1)
        .single()
        .subscribe { String outcome ->

        render outcome
      }
    }
  }
}
