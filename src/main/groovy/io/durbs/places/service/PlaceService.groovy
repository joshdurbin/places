package io.durbs.places.service

import com.lambdaworks.redis.GeoWithin
import io.durbs.places.domain.Place
import rx.Observable

interface PlaceService {

  /**
   *
   * @param place
   * @return
   */
  Observable<Integer> insertPlace(Place place)

  /**
   *
   * @param latitude
   * @param longitude
   * @param searchRadius
   * @return
   */
  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius)

  /**
   *
   * @param latitude
   * @param longitude
   * @param searchRadius
   * @return
   */
  Observable<GeoWithin<Place>> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius)

}