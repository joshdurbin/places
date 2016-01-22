package io.durbs.places.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class MongoConfig {

  String collection
  String db
  String uri
}