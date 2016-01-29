package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.codec.ElasticsearchConversionFunctions
import io.durbs.places.config.ElasticsearchConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import rx.Observable
import rx.functions.Func1

@Singleton
@CompileStatic
@Slf4j
class ElasticsearchPlaceService implements PlaceService {

  final ElasticsearchConfig elasticsearchConfig
  final Client elasticSearchClient

  @Inject
  ElasticsearchPlaceService(ElasticsearchConfig elasticsearchConfig) {

    this.elasticsearchConfig = elasticsearchConfig

    elasticSearchClient = TransportClient.builder().build()
      .addTransportAddress(
      new InetSocketTransportAddress(InetAddress.getByName(elasticsearchConfig.hostname), elasticsearchConfig.hostport))
  }

  @Override
  Observable<Integer> insertPlace(final Place place) {

    Observable.from(elasticSearchClient.index(new IndexRequest(elasticsearchConfig.index, elasticsearchConfig.type).source(ElasticsearchConversionFunctions.CREATE_OBJECT_BUILDER_FOR_PLACE(place))))
      .map({ IndexResponse indexResponse ->

      indexResponse.created ? 1 : 0
    } as Func1)
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    final String query = "{ \"query\" : { \"match_all\" : {} }, \"filter\" : { \"geo_distance\" : { \"distance\" : \"${searchRadius}m\", \"location\" : { \"lat\" : ${latitude}, \"lon\" : ${longitude} } } } }"

    Observable.from(elasticSearchClient.search(new SearchRequest(elasticsearchConfig.index).types(elasticsearchConfig.type).source(query)))
      .flatMap({ final SearchResponse response ->

        Observable.from(response.hits.hits)
      } as Func1)
      .map(ElasticsearchConversionFunctions.MAP_SEARCH_HIT_TO_PLACE)
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {
    return null
  }

}
