package io.durbs.places.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class RethinkConfig {

  String hostname
  Integer port
  String db
  String table
}
