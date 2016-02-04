package io.durbs.places

import com.lambdaworks.redis.GeoWithin
import rx.Observable

interface PlaceService {

  Observable<Integer> insertPlace(Place place)

  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius)

  Observable<GeoWithin<Place>> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius)

}