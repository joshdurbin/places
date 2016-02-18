package io.durbs.places.rethink

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class RethinkConfig {

  static String CONFIG_ROOT = '/rethink'

  String hostname
  Integer port
  String db
  String table
  Integer numOfVertices
  String indexKey
}
