package io.durbs.places.postgres

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoWithin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.Place
import io.durbs.places.PlaceService
import org.jooq.DSLContext
import org.postgis.PGgeometry
import org.postgis.Point
import ratpack.exec.Blocking
import rx.Observable

import static org.jooq.impl.DSL.field

@Singleton
@CompileStatic
@Slf4j
class PostgresPlaceService implements PlaceService {

  @Inject
  DSLContext dslContext

  @Override
  Observable<Integer> insertPlace(Place place) {

//    Blocking.get {
//
//      final Point point = new Point(0, 0)
//      point.setSrid(4326)
//
//      PGgeometry pg
//      dslContext.select(field("thing"), field("thing"))
//
//      dslContext.select(Routines.interse)
//
//      dslContext.resultQuery("").fetch().
//    }

    return null
  }

  @Override
  Observable<Place> getPlaces(Double latitude, Double longitude, Double searchRadius) {

//    Blocking.get {
//
//      dslContext.select(field("thing"), field("thing"))
//
//      dslContext.resultQuery("").fetch().
//    }
//    .observeEach()


    return null
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(Double latitude, Double longitude, Double searchRadius) {
    return null
  }
}
