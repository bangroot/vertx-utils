package com.github.bangroot.vertx.mixins

import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpClient

/**
 * Creator: bangroot
 */
class HttpCall {
  def static log = Logger.getLogger(HttpCall)

  static Map<String, HttpClient> clients = [:]
  static Map<HttpClient, Map<String, String>> sessions = [:]

  HttpClient client
  def body
  Map<String, Object> headers = [:]
  Closure bodyHandler
  Closure responseHandler
  Closure exceptionHandler
  String path
  String sessionCookie
  String auth

  private HttpCall(HttpClient client, String path) {
    this.client = client
    this.path = path
  }

  def static create(Vertx vertx, URL destination) {
    int port = (destination.port > 0) ? destination.port : destination.defaultPort
    def clientKey = "${destination.host}:${port}"
    if (clients.containsKey(clientKey)) {
      return new HttpCall(clients.get(clientKey), destination.file)
    } else {
      log.debug("Creating a new client for ${clientKey}")
      HttpClient client = vertx.createHttpClient([host: destination.host, port: port, maxPoolSize: 1, keepAlive: false])
      if (destination.protocol == "https") client.setSSL(true)
      clients.put(clientKey, client)
      return new HttpCall(client, destination.file)
    }
  }

  def sessionCookie(String cookieName) {
    this.sessionCookie = cookieName
  }

  def Authorization(String auth) {
    this.auth = auth
  }

  def body(def body) {
    this.body = body
  }

  /**
   * When a method is not recognized, assume it is an HTTP Header for the call.
   */
  def methodMissing(String methodName, args) {
    headers[methodName] = args[0]
  }

  def withBody(Closure bodyHandler) {
    this.bodyHandler = bodyHandler
  }

  def withJsonBody(Closure jsonHandler) {
    withBody { body ->
      try {
        def json = new JsonSlurper().parseText(body as String)
        jsonHandler.call(json)
      } catch (JsonException jex) {
        log.error("Error parsing json...throwing away.")
        log.info("Discarded message: \n: $body")
        jsonHandler.call([:])
      }
    }
  }

  def withResponse(Closure responseHandler) {
    this.responseHandler = responseHandler
  }

  def withException(Closure exceptionHandler) {
    this.exceptionHandler = exceptionHandler
  }

  def GET() {
    request('GET')
  }

  def POST() {
    request('POST')
  }

  def PUT() {
    request('PUT')
  }

  def request(String method) {
    log.debug("Requesting to ${path}")
    def request = client.request(method, path) { response ->
      if (sessionCookie && response.statusCode == 401) {
        sessions.remove(client)
        request(method)
      } else {
        responseClosure.call(response)
      }
    }

    if (exceptionHandler) {
      request.exceptionHandler { exception ->
        exceptionHandler.call(exception)
      }
    }

    headers.each { entry ->
      request.putHeader(entry.key, entry.value)
    }

    boolean foundCookie = false
    if (sessionCookie && sessions.containsKey(client)) {
      sessions.get(client).each { entry ->
        if (path.startsWith(entry.key)) {
          log.debug("Adding session cookie: ${entry.value}")
          foundCookie = true
          request.putHeader('Cookie', "${sessionCookie}=${entry.value}")
          //todo smarter cookie finding if this is problematic
        }
      }
    }

    if (!foundCookie && auth) {
      log.debug("Using authorization header: ${auth}")
      request.putHeader('Authorization', auth)
    }

    if (body) {
      request.chunked = true
      use(JsonOutput) {
        log.debug "Sending ${body.toJson()}"
        Buffer bodyBuffer = new Buffer(body.toJson())
        request.write(bodyBuffer)
      }
    }

    request.end()
  }

  def responseClosure = { response ->
    if (sessionCookie) {
      log.debug("Session Cookie set to ${sessionCookie}. Looking for it.")
      response.cookies.each { cookie ->
        log.debug("Got cookie ${cookie}")
        if (cookie.startsWith(sessionCookie)) {
          def cookieParts = cookie.split(";")
          def value = cookieParts[0].split("=")[1]
          def path = "/"
          cookieParts.each { part ->
            if (part.trim().startsWith("Path=")) path = part.split("=")[1]
          }
          log.debug("Found cookie ${sessionCookie} == ${value} on path ${path}")
          if (!sessions.containsKey(client)) sessions.put(client, [:])
          sessions.get(client)[path] = value
        }
      }
    }

    def responseProceed = true
    if (responseHandler) {
      def responseCode = responseHandler.call(response)
      responseProceed = (responseCode == null) ? true : responseCode
    }

    if (bodyHandler && responseProceed) {
      response.bodyHandler { body ->
        bodyHandler.call(body)
      }
    }

  }
}
