package com.github.bangroot.vertx.extensions

import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx
import io.vertx.lang.groovy.GroovyVerticle

class DeployExtension {

  public static void deploy(GroovyVerticle self, @DelegatesTo(Launcher) Closure closure) {
    def launcher = new Launcher(self.vertx)
    closure.delegate = launcher
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
    launcher.execute()
  }

  static class Launcher {
    Vertx vertx
    List deployQueue = []

    public Launcher(Vertx vertx) {
      this.vertx = vertx
    }

    def verticle(String verticleName, Map<String, Object> config = [:], Map<String, Object> options = [instances: 1]) {
      deployQueue << [
          type     : 'verticle',
          name     : verticleName,
          config   : config,
          options: options
      ]
    }

    def worker(String verticleName, Map<String, Object> config = [:], Map<String, Object> options = [instances: 1, worker: true]) {
      options.worker = true
      deployQueue << [
          type         : 'worker',
          name         : verticleName,
          config       : config,
          options: options
      ]
    }

    def execute() {
      use(LoopExtension) {
        deployQueue.loop { target, next ->
          def options = target.options
          options.config = target.config
          vertx.deployVerticle(target.name, options) { result ->
            if (result.failed()) LoggerFactory.getLogger(DeployExtension).fatal("Error loading verticle $target.name", result.cause())
            next()
          }
        }
      }
    }
  }
}
