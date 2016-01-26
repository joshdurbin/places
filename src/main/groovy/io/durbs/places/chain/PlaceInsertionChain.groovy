package io.durbs.places.chain

import com.google.inject.Inject
import com.google.inject.Singleton
import groovy.transform.Immutable
import io.durbs.places.domain.Place
import io.durbs.places.service.impl.ElasticsearchPlaceService
import io.durbs.places.service.impl.MongoPlaceService
import io.durbs.places.service.impl.RedisPlaceService
import io.durbs.places.service.impl.RethinkPlaceService
import ratpack.groovy.handling.GroovyChainAction
import ratpack.jackson.Jackson
import rx.Observable
import rx.functions.Func1

import static ratpack.jackson.Jackson.fromJson

@Singleton
class PlaceInsertionChain extends GroovyChainAction {

  @Inject
  MongoPlaceService mongoPlaceService

  @Inject
  RedisPlaceService redisPlaceService

  @Inject
  RethinkPlaceService rethinkPlaceService

  @Inject
  ElasticsearchPlaceService elasticsearchPlaceService

  @Override
  void execute() throws Exception {

    post {

      parse(fromJson(Place))
        .observe()
        .flatMap ({ final Place place ->

        Observable.zip(
          mongoPlaceService.insertPlace(place).single(),
          redisPlaceService.insertPlace(place).single(),
          rethinkPlaceService.insertPlace(place).single(),
          elasticsearchPlaceService.insertPlace(place).single())
          { Integer mongoInsertionCount, Integer redisInsertionCount, Integer rethinkInsertionCount, Integer elasticsearchInsertionCount ->

            new InsertionResult(mongoInserts: mongoInsertionCount,
              redisInserts: redisInsertionCount,
              rethinkInserts: rethinkInsertionCount,
              elasticsearchInserts: elasticsearchInsertionCount)
          }

      } as Func1)
        .single()
        .subscribe { final InsertionResult insertionResult ->

        render Jackson.json(insertionResult)
      }
    }
  }

  @Immutable
  class InsertionResult {

    Integer mongoInserts
    Integer redisInserts
    Integer rethinkInserts
    Integer elasticsearchInserts
  }
}