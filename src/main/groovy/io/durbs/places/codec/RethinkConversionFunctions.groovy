package io.durbs.places.codec

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.Insert
import io.durbs.places.domain.Place
import rx.functions.Func1

@Singleton
class RethinkConversionFunctions {

  static Func1 MAP_DOCUMENT_TO_PLACE = { final Map rootDocument ->

    final Map placeDocument = rootDocument.get('doc') as Map
    final Map locationDocument = placeDocument.get('location') as Map

    new Place(id: placeDocument.get('id'),
      name: placeDocument.get('name'),
      address: placeDocument.get('address'),
      city: placeDocument.get('city'),
      state: placeDocument.get('state'),
      zipCode: placeDocument.get('zipCode'),
      telephoneNumber: placeDocument.get('telephoneNumber'),
      categories: placeDocument.get('categories') as List,
      neighborhoods: placeDocument.get('neighborhoods') as List,
      latitude: (locationDocument.get('coordinates') as List).last(),
      longitude: (locationDocument.get('coordinates') as List).first())
  } as Func1

  static Insert CREATE_INSERT_COMMAND_FOR_PLACE(RethinkDB rethinkDB, Place place) {

    rethinkDB.table('places')
      .insert(rethinkDB.hashMap('name', place.name)
      .with('address', place.address)
      .with('city', place.city)
      .with('state', place.state)
      .with('zipCode', place.zipCode)
      .with('telephoneNumber', place.telephoneNumber)
      .with('neighborhoods', place.neighborhoods)
      .with('categories', place.categories)
      .with('location', rethinkDB.point(place.longitude, place.latitude)))
  }
}
