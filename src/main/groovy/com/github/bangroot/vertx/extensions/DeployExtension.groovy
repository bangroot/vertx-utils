package com.github.bangroot.vertx.extensions

import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.groovy.platform.Verticle

/**
 * Created by e026391 on 10/31/14.
 */
class DeployExtension {

  public static void launch(Verticle self, @DelegatesTo(Launcher) Closure closure) {
    def launcher = new Launcher(self.getVertx(), self.getContainer())
    closure.delegate = launcher
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
    launcher.execute()
  }

  private static class Launcher {
    Vertx vertx
    Container container

    List deployQueue = []

    public Launcher(Vertx vertx, Container container) {
      this.vertx = vertx
      this.container = container
    }

    def module(String moduleName, Map<String, Object> config = [:], int instances = 1) {
      deployQueue << [
          type     : 'module',
          name     : moduleName,
          config   : config,
          instances: instances
      ]
    }

    def verticle(String verticleName, Map<String, Object> config = [:], int instances = 1) {
      deployQueue << [
          type     : 'verticle',
          name     : verticleName,
          config   : config,
          instances: instances
      ]
    }

    def execute() {
      deployQueue.loop { target, next ->
        switch (target.type) {
          case 'module':
            container.deployModule(target.name, target.config, target.instances) { result ->
              if (result.failed) container.logger.fatal("Error loading module $target.name", result.cause())
              next()
            }
            break;
          case 'verticle':
            container.deployVerticle(target.name, target.config, target.instances) { result ->
              if (result.failed) container.logger.fatal("Error loading verticle $target.name", result.cause())
              next()
            }
            break;
        }
      }
    }
  }
}
