package io.durbs.places

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.bson.BsonDocument
import org.bson.BsonDocumentWrapper
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

@Canonical
@CompileStatic
class Place implements Bson {

  @JsonIgnore
  String id

  String name
  String address
  String city
  String state
  String zipCode
  String telephoneNumber
  List<String> neighborhoods
  List<String> categories
  Double latitude
  Double longitude

  @Override
  <TDocument> BsonDocument toBsonDocument(Class<TDocument> tDocumentClass, CodecRegistry codecRegistry) {

    new BsonDocumentWrapper<Place>(this, codecRegistry.get(Place))
  }
}
