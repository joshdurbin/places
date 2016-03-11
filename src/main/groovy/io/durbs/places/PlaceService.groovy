package io.durbs.places

import rx.Observable

interface PlaceService {

  Observable<Integer> insertPlace(Place place)

  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius)

  Observable<Integer> getNumberOfPlaces()

}