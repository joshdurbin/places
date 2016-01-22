package io.durbs.places.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class RedisConfig {

  String uri
  String geoSetKey
}