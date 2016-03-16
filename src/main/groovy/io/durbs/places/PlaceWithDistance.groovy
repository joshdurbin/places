package io.durbs.places

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class PlaceWithDistance extends Place {

  Double distance
}
