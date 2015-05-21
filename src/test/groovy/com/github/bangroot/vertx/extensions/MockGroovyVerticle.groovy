package com.github.bangroot.vertx.extensions

import io.vertx.groovy.core.Vertx
import io.vertx.lang.groovy.GroovyVerticle

/**
 * Created by e026391 on 12/30/14.
 */
class MockGroovyVerticle extends GroovyVerticle {
  public setVertx(Vertx vertx) {
    this.@vertx = vertx
  }
}
