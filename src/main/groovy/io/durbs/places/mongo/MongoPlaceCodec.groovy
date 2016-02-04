package io.durbs.places.mongo

import io.durbs.places.Place
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext

class MongoPlaceCodec implements CollectibleCodec<Place> {

  static final Codec<Document> documentCodec = new DocumentCodec()

  @Override
  Place generateIdIfAbsentFromDocument(final Place place) {

    if (!documentHasId(place)) {
      place.setId(UUID.randomUUID() as String)
    }

    place
  }

  @Override
  boolean documentHasId(final Place place) {

    place.id
  }

  @Override
  BsonValue getDocumentId(final Place place) {

    if (!documentHasId(place)) {
      throw new IllegalStateException('The place does not contain an _id');
    }

    new BsonString(place.id);
  }

  @Override
  Place decode(BsonReader reader, DecoderContext decoderContext) {

    final Document document = documentCodec.decode(reader, decoderContext)

    new Place(id: document.get('_id') as String,
      name: document.get('name', String),
      address: document.get('address', String),
      city: document.get('city', String),
      state: document.get('state', String),
      zipCode: document.get('zipCode', String),
      telephoneNumber: document.get('telephoneNumber', String),
      categories: document.get('categories', List),
      neighborhoods: document.get('neighborhoods', List),
      latitude: document?.get('loc', Document)?.get('coordinates', List)?.last(),
      longitude: document?.get('loc', Document)?.get('coordinates', List)?.first())
  }

  @Override
  void encode(final BsonWriter writer, final Place place, final EncoderContext encoderContext) {

    final Document document = new Document()

    if (place.name) {
      document.put('name', place.name)
    }

    if (place.address) {
      document.put('address', place.address)
    }

    if (place.city) {
      document.put('city', place.city)
    }

    if (place.state) {
      document.put('state', place.state)
    }

    if (place.zipCode) {
      document.put('zipCode', place.zipCode)
    }

    if (place.telephoneNumber) {
      document.put('telephoneNumber', place.telephoneNumber)
    }

    if (place.neighborhoods) {
      document.put('neighborhoods', place.neighborhoods)
    }

    if (place.categories) {
      document.put('categories', place.categories)
    }

    if (place.latitude && place.longitude) {
      document.put('loc', new Document('type', 'Point').append('coordinates', [place.longitude, place.latitude]))
    }

    documentCodec.encode(writer, document, encoderContext)
  }

  @Override
  Class<Place> getEncoderClass() {
    Place
  }
}
