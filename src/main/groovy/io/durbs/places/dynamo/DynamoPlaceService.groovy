package io.durbs.places.dynamo

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.Place
import io.durbs.places.PlaceService
import rx.Observable

@Singleton
@CompileStatic
@Slf4j
class DynamoPlaceService implements PlaceService {

  @Inject
  PlaceService placeService

  @Override
  Observable<Integer> insertPlace(Place place) {
    return null
  }

  @Override
  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius) {
    return null
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius) {
    return null
  }
}
