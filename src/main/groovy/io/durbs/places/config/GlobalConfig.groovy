package io.durbs.places.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class GlobalConfig {

  Double defaultSearchRadius
  Double maxAllowableSearchRadius
  Long resultSetSize
}
