package io.durbs.places.rtree

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class RTreeConfig {

  static String CONFIG_ROOT = '/rtree'

  Boolean star
  Integer minChildren
  Integer maxChildren
  Integer geoHashBitPrecision
}
