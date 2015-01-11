package com.github.bangroot.vertx.traits

import io.vertx.lang.groovy.GroovyVerticle

/**
 * Creator: bangroot
 */
trait HttpClientSupport {

  def http(String dest, @DelegatesTo(HttpCall) Closure closure) {
    def call = HttpCall.create(vertx, new URL(dest)).
    closure.delegate = call
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }

}
