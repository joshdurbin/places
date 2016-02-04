package io.durbs.places.redis

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class RedisConfig {

  static String CONFIG_ROOT = '/redis'

  String uri
  String geoSetKey
}