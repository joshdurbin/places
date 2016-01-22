@Grapes([
  @Grab(group='com.univocity', module='univocity-parsers', version='1.5.6'),
  @Grab(group='com.fasterxml.jackson.core', module='jackson-databind', version='2.6.2')
])

import groovy.transform.Canonical
import com.univocity.parsers.tsv.*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@Canonical
class Place {

  String name
  String address
  String city
  String state
  String zipCode
  String telephoneNumber
  List<String> neighborhoods
  List<String> categories
  Double latitude
  Double longitude
}

def parser = new TsvParser(new TsvParserSettings())

def jsonMapper = new ObjectMapper()
jsonMapper.setSerializationInclusion(Include.NON_NULL)

def bayareacities = ['Alameda','Albany','American Canyon','Antioch','Atherton','Belmont','Belvedere','Benicia','Berkeley','Brentwood','Brisbane','Burlingame','Calistoga','Campbell','Clayton','Cloverdale','Colma','Concord','Corte Madera','Cotati','Cupertino','Daly City','Danville','Dixon','Dublin','East Palo Alto','El Cerrito','Emeryville','Fairfax','Fairfield','Foster City','Fremont','Gilroy','Half Moon Bay','Hayward','Healdsburg','Hercules','Hillsborough','Lafayette','Larkspur','Livermore','Los Altos','Los Altos Hills','Los Gatos','Martinez','Menlo Park','Mill Valley','Millbrae','Milpitas','Monte Sereno','Moraga','Morgan Hill','Mountain View','Napa','Newark','Novato','Oakland','Oakley','Orinda','Pacifica','Palo Alto','Petaluma','Piedmont','Pinole','Pittsburg','Pleasant Hill','Pleasanton','Portola Valley','Redwood City','Richmond','Rio Vista','Rohnert Park','Ross','St. Helena','San Anselmo','San Bruno','San Carlos','San Francisco','San Jose','San Leandro','San Mateo','San Pablo','San Rafael','San Ramon','Santa Clara','Santa Rosa','Saratoga','Sausalito','Sebastopol','Sonoma','South San Francisco','Suisun City','Sunnyvale','Tiburon','Union City','Vacaville','Vallejo','Walnut Creek','Windsor','Woodside','Yountville']

new File('').eachLine { line, lineNumber ->

  if (lineNumber > 1) {

    def parsedValues = parser.parseLine(line)

    if (parsedValues[1] && parsedValues[13] && parsedValues[14] && parsedValues[5]) {

      if (bayareacities.contains(parsedValues[5]) && parsedValues[6] == 'CA') {

        def place = new Place(
          name: parsedValues[1],
          address: parsedValues[2],
          city: parsedValues[5],
          state: parsedValues[6],
          zipCode: parsedValues[9],
          telephoneNumber: parsedValues[11],
          neighborhoods: parsedValues[15]?.replace('[','')?.replace(']','')?.replace('"','')?.split(','),
          categories: parsedValues[19]?.replace('[','')?.replace(']','')?.replace('"','')?.split(','),
          latitude: Double.valueOf(parsedValues[13]),
          longitude: Double.valueOf(parsedValues[14])
        )

        println jsonMapper.writeValueAsString(place)
      }
    }
  }
}
