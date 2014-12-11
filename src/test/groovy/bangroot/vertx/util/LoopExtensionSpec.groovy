package bangroot.vertx.util

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class LoopExtensionSpec extends Specification {

  @Shared
  def mock

  def "Basic List Loop"() {
    given:
    def loops = [1, 2, 3]
    when:

    def values = []
    use(LoopExtension) {
      loops.loop { element, next -> values << element; next(); }
    }
    then:
    assert values == [1, 2, 3]

  }

  @Unroll
  def "Loops #input (#input.class.name) and final #finalLoop"() {
    given:
    mock = Mock(OutputHolder)

    when:

    def values = []
    use(LoopExtension) {
      if (finalLoop) {
        input.loop({ element, next -> values << element; next(); }, finalLoop)
      } else {
        input.loop { element, next -> values << element; next(); }
      }
    }
    then:
    assert values == input
    if (finalLoop) {
      1 * mock.setOutput(_)
    }

    where:

    input                  | finalLoop
    [1, 2, 3]              | null
    [1, 2, 3]              | { mock.setOutput(true) }
    [1, 2, 3] as Integer[] | null
    [1, 2, 3] as Integer[] | { mock.setOutput(true) }
  }

  @Unroll
  def "Loops on Maps #input (#input.class.name) and final #finalLoop"() {
    given:
    mock = Mock(OutputHolder)

    when:

    def values = [:]
    use(LoopExtension) {
      if (finalLoop) {
        input.loop({ key, value, next -> values[key] = value; next(); }, finalLoop)
      } else {
        input.loop { key, value, next -> values[key] = value; next(); }
      }
    }
    then:
    assert values == input
    if (finalLoop) {
      1 * mock.setOutput(_)
    }

    where:

    input              | finalLoop
    [a: 1, b: 2, c: 3] | null
    [a: 1, b: 2, c: 3] | { mock.setOutput(true) }
  }

  interface OutputHolder {
    def setOutput(value)

    def getOutput()
  }

}

