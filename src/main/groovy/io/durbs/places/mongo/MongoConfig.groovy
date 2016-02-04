package io.durbs.places.mongo

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class MongoConfig {

  static String CONFIG_ROOT = '/mongo'

  String collection
  String db
  String uri
}