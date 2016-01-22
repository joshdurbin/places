@Grapes([
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.2')
])

import groovyx.net.http.AsyncHTTPBuilder
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON

def http = new AsyncHTTPBuilder(
  poolSize : 2,
  uri : 'http://localhost:5050/insert',
  contentType : JSON )

new File('bayareaplaces.json').eachLine { line ->

  http.request(POST) { req ->
    body = line
    requestContentType = JSON

    response.success = { resp ->
      println "Success! ${resp.status}"
    }

    response.failure = { resp ->
      println "Request failed with status ${resp.status}"
    }
  }
}
