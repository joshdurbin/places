def slurper = new groovy.json.JsonSlurper()

new File('bayareaplaces.json').eachLine { line ->
  def json = slurper.parseText(line)
  println "\$(URL)/${json.latitude}/${json.longitude}/250"
}
