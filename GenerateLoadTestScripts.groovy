def slurper = new groovy.json.JsonSlurper()

def distanceOptionsInMeters = [25, 50, 100, 250, 500, 1000]

def redisWriter = new File('bayareaplaces_redis_loadtest.txt').newWriter()
def mongoWriter = new File('bayareaplaces_mongo_loadtest.txt').newWriter()
def elasticWriter = new File('bayareaplaces_elastic_loadtest.txt').newWriter()
def rethinkWriter = new File('bayareaplaces_rethink_loadtest.txt').newWriter()

def baseURL = 'http://localhost:5050'

redisWriter.writeLine("URL=${baseURL}/redis")
mongoWriter.writeLine("URL=${baseURL}/mongo")
elasticWriter.writeLine("URL=${baseURL}/elastic")
rethinkWriter.writeLine("URL=${baseURL}/rethink")

new File('bayareaplaces.json').eachLine { line ->

  def json = slurper.parseText(line)
  def random = new java.util.Random().nextInt(5)

  def url = "\$(URL)/${json.latitude}/${json.longitude}/${distanceOptionsInMeters.get(random)}"

  redisWriter.writeLine(url)
  mongoWriter.writeLine(url)
  elasticWriter.writeLine(url)
  rethinkWriter.writeLine(url)
}
