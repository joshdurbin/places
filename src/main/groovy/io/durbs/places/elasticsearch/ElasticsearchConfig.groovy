package io.durbs.places.elasticsearch

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ElasticsearchConfig {

  static String CONFIG_ROOT = '/elastic'

  String index
  String type
  String hostname
  Integer hostport
  String geoIndexTypeHintJSON
}