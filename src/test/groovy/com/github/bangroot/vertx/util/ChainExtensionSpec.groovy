package com.github.bangroot.vertx.util

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChainExtensionSpec extends Specification {

  @Shared
  def mock

  def "Basic Chaining"() {
    given:
    def handler = new Handler()
    when:
    def calledVar = false
    handler.chain { next -> next() } { next -> calledVar = true }
    then:
    assert calledVar
  }

  @Unroll
  def "Basic Chaining of #input -> chain of #chain.size()"() {
    given:
    mock = Mock(OutputHolder)
    def handler = new Handler();

    when:
    if (input) {
      if (input instanceof Collection) {
        def call = input + chain
        handler.chain(*call)
      } else {
        handler.chain(input, *chain)
      }
    } else {
      handler.chain(*chain)
    }

    then:
    1 * mock.setOutput(_)

    where:
    input    | chain
    null     | [{ next -> mock.output = true }]
    null     | [{ next -> next() }, { next -> mock.output = true }]
    10       | [{ input, next -> assert input == 10; next() }, { next -> mock.output = true }]
    [10, 20] | [{ arg1, arg2, next -> assert arg1 == 10; assert arg2 == 20; next() }, { next -> mock.output = true }]
  }

  interface OutputHolder {
    def setOutput(def value)

    def getOutput()
  }

  @Mixin(ChainExtension)
  class Handler {

  }
}

