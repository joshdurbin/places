package io.durbs.places.rtree

import ch.hsr.geohash.GeoHash
import com.github.davidmoten.grumpy.core.Position
import com.github.davidmoten.rtree.RTree
import com.github.davidmoten.rtree.geometry.Geometries
import com.github.davidmoten.rtree.geometry.Point
import com.github.davidmoten.rtree.geometry.Rectangle
import com.github.davidmoten.rtree.Entry
import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.GeoCoordinates
import com.lambdaworks.redis.GeoWithin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.GlobalConfig
import io.durbs.places.Place
import io.durbs.places.PlaceService
import rx.Observable
import rx.Subscriber
import rx.functions.Func1
import rx.subjects.PublishSubject

@Singleton
@CompileStatic
@Slf4j
class RTreePlaceService implements PlaceService {

  final GlobalConfig globalConfig
  final RTreeConfig rTreeConfig

  final PublishSubject<Entry<Place,Point>> subject
  volatile RTree<Place,Point> tree

  @Inject
  RTreePlaceService(final GlobalConfig globalConfig, final RTreeConfig rTreeConfig) {

    this.globalConfig = globalConfig
    this.rTreeConfig = rTreeConfig

    RTree.Builder rTreeBuilder = RTree.minChildren(rTreeConfig.minChildren).maxChildren(rTreeConfig.maxChildren)

    if (rTreeConfig.star) {
      rTreeBuilder = rTreeBuilder.star()
    }

    subject = PublishSubject.create()
    subject.subscribe(new Subscriber<Entry<Place,Point>>() {

      @Override
      void onCompleted() {

      }

      @Override
      void onError(Throwable e) {

        log.error(e.getMessage(), e)
      }

      @Override
      void onNext(final Entry<Place, Point> entry) {

        log.debug("Adding place w/ name '${entry.value().name}' to tree with coordinates [${entry.value().latitude},${entry.value().longitude}]")
        tree = tree.add(entry)
      }
    })

    tree = rTreeBuilder.create()
  }

  @Override
  Observable<Integer> insertPlace(final Place place) {

    subject.onNext(new Entry<Place, Point>(place, Geometries.pointGeographic(place.longitude, place.latitude)))

    Observable.defer({

      Observable.just(tree.size())
    }).bindExec()
  }

  @Override
  Observable<Place> getPlaces(final Double latitude, final Double longitude, final Double searchRadius) {

    search(tree, Geometries.pointGeographic(longitude, latitude), searchRadius / 1000)
      .map( { final Entry<Place, Point> entry ->

      entry.value()
    } as Func1)
    .bindExec()
  }

  @Override
  Observable<GeoWithin<Place>> getPlacesWithDistance(final Double latitude, final Double longitude, final Double searchRadius) {

    final Point queryPoint = Geometries.pointGeographic(longitude, latitude)

    search(tree, queryPoint, searchRadius / 1000)
      .map( { final Entry<Place, Point> entry ->

      final Place place = entry.value()

      new GeoWithin<Place>(place, entry.geometry().distance(queryPoint), GeoHash.withBitPrecision(place.latitude, place.longitude, rTreeConfig.geoHashBitPrecision).longValue(), new GeoCoordinates(place.latitude, place.longitude))
    } as Func1)
    .bindExec()
  }

  @Override
  Observable<Integer> getNumberOfPlaces() {

    Observable.just(tree.size())
      .bindExec()
  }

  public static Observable<Entry<Place, Point>> search(final RTree<Place, Point> tree, final Point lonLat, final double distanceKm) {

    final Position from = Position.create(lonLat.y(), lonLat.x())
    final Rectangle bounds = createBounds(from, distanceKm)

    tree
      .search(bounds)
      .filter(new Func1<Entry<Place, Point>, Boolean>() {

        @Override
        public Boolean call(Entry<Place, Point> entry) {

          final Point p = entry.geometry()
          final Position position = Position.create(p.y(), p.x())

          from.getDistanceToKm(position) < distanceKm
        }
    })
  }

  private static Rectangle createBounds(final Position from, final double distanceKm) {

    final Position north = from.predict(distanceKm, 0)
    final Position south = from.predict(distanceKm, 180)
    final Position east = from.predict(distanceKm, 90)
    final Position west = from.predict(distanceKm, 270)

    Geometries.rectangle(west.getLon(), south.getLat(), east.getLon(), north.getLat())
  }

}
