package io.durbs.places.codec

import io.durbs.places.domain.Place
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.search.SearchHit
import rx.functions.Func1

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder

@Singleton
class ElasticsearchConversionFunctions {

  static Func1 MAP_SEARCH_HIT_TO_PLACE = { final SearchHit hit ->

    new Place(
      name: hit.source.get('name') as String,
      address: hit.source.get('address') as String,
      city: hit.source.get('city') as String,
      state: hit.source.get('state') as String,
      zipCode: hit.source.get('zipCode') as String,
      telephoneNumber: hit.source.get('telephoneNumber') as String,
      categories: hit.source.get('categories') as List,
      neighborhoods: hit.source.get('neighborhoods') as List,
      latitude: (hit.source.get('location') as List)?.last(),
      longitude: (hit.source.get('location') as List)?.first())
  } as Func1

  static XContentBuilder CREATE_OBJECT_BUILDER_FOR_PLACE(final Place place) {

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
      builder.field('location', [place.longitude, place.latitude])
    }

    builder.endObject()

    builder
  }
}
