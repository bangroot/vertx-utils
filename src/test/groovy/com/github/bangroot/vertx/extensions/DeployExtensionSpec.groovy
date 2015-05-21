package com.github.bangroot.vertx.extensions

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.groovy.core.Vertx
import io.vertx.lang.groovy.GroovyVerticle
import spock.lang.Specification

/**
 * Created by bangroot on 12/11/14.
 */
class DeployExtensionSpec extends Specification {

  def "Test Basic Verticle Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        verticle("TestVerticle")
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 1, config: [:]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Multiple Verticle Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        verticle("TestVerticle")
        verticle("TestVerticle2")
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 1, config: [:]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle2", [instances: 1, config: [:]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Verticle Deploy with Config"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        verticle("TestVerticle", [:], [instances:2])
        verticle("TestVerticle2", [config: true])
        verticle("TestVerticle3", [config: true], [instances:2])
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 2, config: [:]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle2", [instances: 1, config: [config: true]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle3", [instances: 2, config: [config: true]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Basic Worker Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        worker("TestVerticle")
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 1, config: [:], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Multiple Worker Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        worker("TestVerticle")
        worker("TestVerticle2")
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 1, config: [:], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle2", [instances: 1, config: [:], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Worker Deploy with Config"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        worker("TestVerticle", [:], [instances: 2])
        worker("TestVerticle2", [config: true])
        worker("TestVerticle3", [config: true], [instances: 2])
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 2, config: [:], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle2", [instances: 1, config: [config: true], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle3", [instances: 2, config: [config: true], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }

  def "Test Mixed Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockVerticle = new MockGroovyVerticle()
    mockVerticle.vertx = mockVertx

    when:
    use(DeployExtension) {
      mockVerticle.deploy {
        verticle("TestVerticle", [config: true], [instances: 2])
        worker("TestVerticle2", [config: true], [instances: 2])
      }
    }

    then:
    1 * mockVertx.deployVerticle("TestVerticle", [instances: 2, config: [config: true]], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
    1 * mockVertx.deployVerticle("TestVerticle2", [instances: 2, config: [config: true], worker: true], _ as Handler<AsyncResult>) >> { name, options, Handler<AsyncResult> handler ->
      handler.handle(Future.succeededFuture())
    }
  }


}
