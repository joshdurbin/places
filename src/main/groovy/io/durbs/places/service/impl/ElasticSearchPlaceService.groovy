package io.durbs.places.service.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import io.durbs.places.config.ElasticsearchConfig
import io.durbs.places.domain.Place
import io.durbs.places.service.PlaceService
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.search.SearchHit
import rx.Observable
import rx.functions.Func1

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder

@Singleton
//@CompileStatic
class ElasticsearchPlaceService implements PlaceService {

  @Inject
  Client elasticSearchClient

  @Inject
  ElasticsearchConfig elasticsearchConfig

  @Override
  Observable<Integer> insertPlace(Place place) {

    final XContentBuilder builder = jsonBuilder().startObject()

    if (place.name) {
      builder.field('name', place.name)
    }

    if (place.address) {
      builder.field('address', place.address)
    }

    if (place.city) {
      builder.field('city', place.city)
    }

    if (place.state) {
      builder.field('state', place.state)
    }

    if (place.zipCode) {
      builder.field('zipCode', place.zipCode)
    }

    if (place.telephoneNumber) {
      builder.field('telephoneNumber', place.telephoneNumber)
    }

    if (place.neighborhoods) {
      builder.field('neighborhoods', place.neighborhoods)
    }

    if (place.categories) {
      builder.field('categories', place.categories)
    }

    if (place.latitude && place.longitude) {
      builder.field('categories', [place.longitude, place.latitude])
    }

    builder.endObject()

    Observable.from(elasticSearchClient.index(new IndexRequest("places", "place").source(builder)))
      .map({ IndexResponse indexResponse ->

      indexResponse.created ? 1 : 0
    } as Func1)
  }

  @Override
  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius) {

    final String query = "{ \"query\" : { \"match_all\" : {} }, \"filter\" : { \"geo_distance\" : { \"distance\" : \"10km\", \"location\" : { \"lat\" : ${latitude}, \"lon\" : ${longitude} } } } }"

    Observable.from(elasticSearchClient.search(new SearchRequest(elasticsearchConfig.index).types(elasticsearchConfig.type).source(query)))
      .flatMap { SearchResponse response ->

      Observable.from(response.hits.hits)
    }.map { SearchHit hit ->

      new Place(
        name: hit.source.get('name') as String,
        address: hit.source.get('address') as String,
        city: hit.source.get('city') as String,
        state: hit.source.get('state') as String,
        zipCode: hit.source.get('zipCode') as String,
        telephoneNumber: hit.source.get('telephoneNumber') as String,
        categories: hit.source.get('categories') as List,
        neighborhoods: hit.source.get('neighborhoods') as List,
        latitude: hit.source.get('location')?.last(),
        longitude: hit.source.get('location')?.first())
    }
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius) {
    return null
  }

  @Override
  void prepareDatastore() {

  }
}
