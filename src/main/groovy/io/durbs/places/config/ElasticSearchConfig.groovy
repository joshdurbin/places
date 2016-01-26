package io.durbs.places.config

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ElasticsearchConfig {

  String index
  String type
  String hostname
  Integer hostport
}
