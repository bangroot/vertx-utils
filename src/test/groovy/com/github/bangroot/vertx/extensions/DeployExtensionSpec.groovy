package com.github.bangroot.vertx.extensions

import org.vertx.groovy.core.Vertx
import org.vertx.groovy.platform.Container
import org.vertx.groovy.platform.Verticle
import spock.lang.Specification

/**
 * Created by bangroot on 12/11/14.
 */
class DeployExtensionSpec extends Specification {

  def "Test Basic Verticle Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        verticle("TestVerticle")
      }
    }

    then:
    1 * mockContainer.deployVerticle("TestVerticle", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Multiple Verticle Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        verticle("TestVerticle")
        verticle("TestVerticle2")
      }
    }

    then:
    1 * mockContainer.deployVerticle("TestVerticle", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployVerticle("TestVerticle2", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Verticle Deploy with Config"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        verticle("TestVerticle", [:], 2)
        verticle("TestVerticle2", [config: true])
        verticle("TestVerticle3", [config: true], 2)
      }
    }

    then:
    1 * mockContainer.deployVerticle("TestVerticle", [:], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployVerticle("TestVerticle2", [config: true], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployVerticle("TestVerticle3", [config: true], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Basic Worker Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        worker("TestVerticle")
      }
    }

    then:
    1 * mockContainer.deployWorkerVerticle("TestVerticle", [:], 1, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
  }

  def "Test Multiple Worker Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        worker("TestVerticle")
        worker("TestVerticle2")
      }
    }

    then:
    1 * mockContainer.deployWorkerVerticle("TestVerticle", [:], 1, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployWorkerVerticle("TestVerticle2", [:], 1, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
  }

  def "Test Worker Deploy with Config"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        worker("TestVerticle", [:], 2)
        worker("TestVerticle2", [config: true])
        worker("TestVerticle3", [config: true], 2)
      }
    }

    then:
    1 * mockContainer.deployWorkerVerticle("TestVerticle", [:], 2, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployWorkerVerticle("TestVerticle2", [config: true], 1, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployWorkerVerticle("TestVerticle3", [config: true], 2, false, _ as Closure) >> { name, config, instances, multithreaded, closure ->
      closure([failed: false])
    }
  }

  def "Test Basic Module Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        module("TestModule")
      }
    }

    then:
    1 * mockContainer.deployModule("TestModule", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Multiple Module Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        module("TestModule")
        module("TestModule2")
      }
    }

    then:
    1 * mockContainer.deployModule("TestModule", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployModule("TestModule2", [:], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Module Deploy with Config"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        module("TestModule", [:], 2)
        module("TestModule2", [config: true])
        module("TestModule3", [config: true], 2)
      }
    }

    then:
    1 * mockContainer.deployModule("TestModule", [:], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployModule("TestModule2", [config: true], 1, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployModule("TestModule3", [config: true], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }

  def "Test Mixed Deploy"() {
    given:
    def mockVertx = Mock(Vertx)
    def mockContainer = Mock(Container)
    def mockVerticle = Mock(Verticle)
    mockVerticle.getVertx() >> mockVertx
    mockVerticle.getContainer() >> mockContainer

    when:
    use(DeployExtension) {
      mockVerticle.launch {
        module("TestModule", [:], 2)
        verticle("TestVerticle", [config: true], 2)
      }
    }

    then:
    1 * mockContainer.deployModule("TestModule", [:], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
    1 * mockContainer.deployVerticle("TestVerticle", [config: true], 2, _ as Closure) >> { name, config, instances, closure ->
      closure([failed: false])
    }
  }


}
