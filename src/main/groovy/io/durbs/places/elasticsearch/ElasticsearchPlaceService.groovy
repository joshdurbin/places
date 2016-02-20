package io.durbs.places.elasticsearch

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.GlobalConfig
import io.durbs.places.Place
import io.durbs.places.PlaceService
import org.elasticsearch.action.count.CountRequestBuilder
import org.elasticsearch.action.count.CountResponse
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.unit.DistanceUnit
import rx.Observable
import rx.functions.Func1

import static org.elasticsearch.index.query.QueryBuilders.geoDistanceQuery

@Singleton
@CompileStatic
@Slf4j
class ElasticsearchPlaceService implements PlaceService {

  @Inject
  ElasticsearchConfig elasticsearchConfig

  @Inject
  Client elasticSearchClient

  @Inject
  GlobalConfig globalConfig

  @Override
  Observable<Integer> insertPlace(final Place place) {

    final IndexRequestBuilder builder = elasticSearchClient
      .prepareIndex(elasticsearchConfig.index, elasticsearchConfig.type)
      .setSource(ElasticsearchConversionFunctions.CREATE_OBJECT_BUILDER_FOR_PLACE(place))

    // CONVERT THE FUTURE TO OBSERVABLE STREAM, IN WHICH ONE ITEM IS EMITTED : INDEX RESPONSE
    Observable.from(builder.execute())
      .map({ final IndexResponse indexResponse ->

      indexResponse.created ? 1 : 0
    } as Func1)
    .bindExec()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    final SearchRequestBuilder builder = elasticSearchClient
      .prepareSearch(elasticsearchConfig.index)
      .setTypes(elasticsearchConfig.type)
      .setQuery(geoDistanceQuery('location')
        .distance(searchRadius, DistanceUnit.METERS)
        .lat(latitude)
        .lon(longitude))
      .setSize(globalConfig.resultSetSize as Integer)

    // CONVERT THE FUTURE TO OBSERVABLE STREAM, IN WHICH ONE ITEM IS EMITTED : SEARCH RESPONSE
    Observable.from(builder.execute())
      .flatMap({ final SearchResponse response ->

      // EMIT A NEW STREAM OF SEARCH HIT
      Observable.from(response.hits.hits)
    } as Func1)
    .map(ElasticsearchConversionFunctions.MAP_SEARCH_HIT_TO_PLACE)
    .bindExec()
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {
    return null
  }

  @Override
  Observable<Integer> getNumberOfPlaces() {

    final CountRequestBuilder builder = elasticSearchClient
      .prepareCount(elasticsearchConfig.index)
      .setTypes(elasticsearchConfig.type)

    // CONVERT THE FUTURE TO OBSERVABLE STREAM, IN WHICH ONE ITEM IS EMITTED : COUNT REQUEST BUILDER
    Observable.from(builder.execute())
      .map( { final CountResponse countResponse ->

      countResponse.count as Integer

    } as Func1)
    .bindExec()
  }
}
