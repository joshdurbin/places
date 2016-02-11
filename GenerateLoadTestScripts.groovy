def slurper = new groovy.json.JsonSlurper()

def distanceOptionsInMeters = [25, 50, 100, 250, 500, 1000]

def queryLoadtestWriter = new File('places_query_loadtest_urls.txt').newWriter()
def insertLoadtestWriter = new File('places_insert_loadtest_urls.txt').newWriter() 

def baseURL = 'http://localhost:5050'

queryLoadtestWriter.writeLine("URL=${baseURL}/places")
insertLoadtestWriter.writeLine("URL=${baseURL}/places")

new File('bayareaplaces.json').eachLine { line ->

  def json = slurper.parseText(line)
  def random = new java.util.Random().nextInt(5)

  def url = "\$(URL)/${json.latitude}/${json.longitude}/${distanceOptionsInMeters.get(random)}"

  queryLoadtestWriter.writeLine(url)

  // using this instead of something like -- awk '{print "$(URL) POST " $0}' bayareaplaces.json > places_insert_loadtest_urls.txt
  insertLoadtestWriter.writeLine("\$(URL) POST ${line}")
}
