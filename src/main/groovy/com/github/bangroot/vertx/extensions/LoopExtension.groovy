package com.github.bangroot.vertx.extensions
/**
 * <p>Groovy category to add loop semantics similar to {@link Object#each(Closure)} but with an additional closure which
 * executes after the looping is complete. This construct is handy in reactive programming models, especially when
 * combined with {@link ChainExtension chaining}.</p>
 *
 * <p><b>Note:</b>The LoopCategory is <b>not</b> safe to use with the {@code @Mixin} annotation. It must be used with either a
 * {@code use ( )} block or via a groovy compiler AST transformation.</p>
 *
 * <h4>Usage</h4>
 * <p>Loop functions work much like {@code each} closures with two notable exceptions. First, you must explicitly call
 * {@code next ( )} to move processing forward in the loop:</p>
 *
 * <pre>
 *  use(LoopCategory) {*    [1,2,3].loop {element, next -> println element; next();}*}* </pre>
 *
 * <p>While this may look like additional overhead, it simplifies logic when the looping closure has complex branching or
 * involves a chain. The second key difference is the addition of an optional final closure to be invoked after the last
 * element has been processed.</p>
 *
 * <pre>
 *   use(LoopCategory) {*     [1,2,3].loop {element, next -> println element; next();}{println "done!"}*}* </pre>
 *
 * <p>Looping also works with maps, but key and value are separated for you:</p>
 *
 * <pre>
 *  use(LoopCategory) {*    [a:1,b:2,c:3].loop {key, value, next -> println "${key} = ${value}"; next();}*}* </pre>
 */
class LoopExtension {
  /**
   * Implementation of {@code loop} semantics for arrays with no final closure
   * @param array Array to be iterated
   * @param action Closure to be executed for each element with two inputs {@code element} and {@code next}
   */
  static void loop(final Object[] array, final Closure action) {
    loop(array, action, {})
  }

  /**
   * Implementation of {@code loop} semantics for arrays with supplied final closure
   * @param array Array to be iterated
   * @param action Closure to be executed for each element with two inputs {@code element} and {@code next}
   * @param next Closure to be executed on after the final call. No inputs are supplied to the final closure.
   */
  static void loop(final Object[] array, final Closure action, final Closure next) {
    _loop(array?.iterator(), action, next)
  }

  /**
   * Implementation of {@code loop} semantics for Collections with no final closure
   * @param collection Collection to be iterated
   * @param action Closure to be executed for each element with two inputs {@code element} and {@code next}
   */
  static void loop(final Collection collection, final Closure action) {
    loop(collection, action, {})
  }

  /**
   * Implementation of {@code loop} semantics for Collections with supplied final closure
   * @param collection Collection to be iterated
   * @param action Closure to be executed for each element with two inputs {@code element} and {@code next}
   * @param next Closure to be executed on after the final call. No inputs are supplied to the final closure.
   */
  static void loop(final Collection collection, final Closure action, final Closure next) {
    _loop(collection.iterator(), action, next)
  }

  /**
   * Implementation of {@code loop} semantics for Maps with no final closure
   * @param map Map to be iterated
   * @param action Closure to be executed for each element with three inputs {@code key}, {@code value}, and
   * {@code next}
   */
  static void loop(final Map map, final Closure action) {
    loop(map, action, {})
  }

  /**
   * Implementation of {@code loop} semantics for Maps with supplied final closure
   * @param map Map to be iterated
   * @param action Closure to be executed for each element with three inputs {@code key}, {@code value}, and
   * {@code next}
   * @param next Closure to be executed on after the final call. No inputs are supplied to the final closure.
   */
  static void loop(final Map map, final Closure action, final Closure next) {
    _loop(map.iterator(), action, next)
  }

  private static void _loop(final Iterator<?> iterator, final Closure action, Closure next = {}) {
    if (iterator) {
      def element = iterator.next()
      def nextAction
      if (iterator) nextAction = LoopExtension.&_loop.curry(iterator, action, next) else nextAction = next
      if (element instanceof Map.Entry) action.call(element.key, element.value, nextAction) else action.call(element, nextAction)
    } else next.call()
  }
}
