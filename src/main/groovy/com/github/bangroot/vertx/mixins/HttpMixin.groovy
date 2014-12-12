package com.github.bangroot.vertx.mixins

import org.vertx.groovy.platform.Verticle

/**
 * Creator: bangroot
 */
@Category(Verticle)
class HttpMixin {

  def http(String dest, @DelegatesTo(HttpCall) Closure closure) {
    def call = HttpCall.create(vertx, new URL(dest))
    closure.delegate = call
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }

}
