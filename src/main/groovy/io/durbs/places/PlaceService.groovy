package io.durbs.places

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
   * @param maxResultSize
   * @return
   */
  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius, Integer maxResultSize)

  /**
   *
   * @param latitude
   * @param longitude
   * @param searchRadius
   * @param maxResultSize
   * @return
   */
  Observable<PlaceWithDistance> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius, Integer maxResultSize)

  /**
   *
   * @return
   */
  Observable<Integer> getNumberOfPlaces()

}