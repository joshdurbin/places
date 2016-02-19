package io.durbs.places.rethink

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.Insert
import io.durbs.places.Place
import rx.functions.Func1

@Singleton
class RethinkConversionFunctions {

  static Func1 MAP_DOCUMENT_TO_PLACE = { final Map rootDocument ->

    final Map locationDocument = rootDocument.get('location') as Map

    new Place(id: rootDocument.get('id'),
      name: rootDocument.get('name'),
      address: rootDocument.get('address'),
      city: rootDocument.get('city'),
      state: rootDocument.get('state'),
      zipCode: rootDocument.get('zipCode'),
      telephoneNumber: rootDocument.get('telephoneNumber'),
      categories: rootDocument.get('categories') as List,
      neighborhoods: rootDocument.get('neighborhoods') as List,
      latitude: (locationDocument.get('coordinates') as List).last(),
      longitude: (locationDocument.get('coordinates') as List).first())
  } as Func1

  static Insert CREATE_INSERT_COMMAND_FOR_PLACE(final RethinkDB rethinkDB, final String tableName, final Place place) {

    rethinkDB.table(tableName)
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
