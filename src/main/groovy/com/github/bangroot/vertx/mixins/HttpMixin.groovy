package com.github.bangroot.vertx.mixins

/**
 * Creator: bangroot
 */
class HttpMixin {

  def http(String dest, @DelegatesTo(HttpCall) Closure closure) {
    def call = HttpCall.create(vertx, new URL(dest))
    closure.delegate = call
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }

}
