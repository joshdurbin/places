package io.durbs.places

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class GlobalConfig {

  static String CONFIG_ROOT = '/global'

  Double defaultSearchRadius
  Double maxAllowableSearchRadius
  Long resultSetSize
  Datastore datastoreTarget

  enum Datastore { elastic, mongo, redis, rethink, rtree }
}
