package com.github.bangroot.vertx.extensions

/**
 * <p>Mixin to add chaining support for groovy/reactive applications. Inspired by work from Sascha Klein
 * (<a href="http://www.slideshare.net/sascha_klein/vertx-using-groovy">slides</a>,
 * <a href=" https://www.youtube.com/watch?v=dsRYKgNz55o&list=FLZcybLPmV_qCz2XObpUPjMg">youtube</a>)</p>
 *
 * <p>This Mixin can safely be mixed into any class using {@code @Mixin(ChainMixin) }</p>
 *
 * <h4>Usage</h4>
 * <p>The chain functionality is used to alleviate callback hell when trying to chain multiple calls together in a logical
 * order. This does not support lots of branches in logic, but chains could be nested to give this effect. A basic chain
 * (with no inputs) would look like:</p>
 *
 * <pre>
 *     chain {next -> do_something(); next() }, { next -> something_else(); message.reply()}* </pre>
 *
 * <p><b>Note:</b> The call to {@code next ( )} is required to continue the chain. The final call doesn't not attempt to
 * call the next closure. {@code next} will always be the last argument to the closure (inputs are the first {@code n}
 * arguments.
 *
 * <p>Inputs can be passed into the start of a chain as the first parameters to the call:</p>
 *
 * <pre>
 *     chain 10, {input, next -> print_int(input); next() }, { next -> something_else(); message.reply()}* </pre>
 *
 * <p>in which case, the {@code print_int} function would be called passing in 10.
 * Multiple inputs are also supported:</p>
 *
 * <pre>
 *     chain 10, 20, {input_10, input_20, next -> print_int(input_10); next() }, { next -> something_else(); message.reply()}* </pre>
 *
 * <p>Finally, inputs may be passed into the next step in the chain:</p>
 *
 * <pre>
 *     chain 10, 20, {input_10, input_20, next -> print_int(input_10); next(input_20) }, { input, next -> print_int(input); message.reply()}* </pre>
 *
 * <p>This model allows for closures and logic to easily be built in a componentized manner:</p>
 *
 * <pre>
 *     chain 10, 20, some_closure, another_closure
 * </pre>
 *
 */
class ChainExtension {
  /**
   * Implementation of {@code chain} semantics in which there is 0 or 1 input argument.
   * @param arguments input arguments to the first closure in the chain (or the first closure if no inputs)
   * @param actions closures to chain together
   */
  public static void chain(Object self, def arguments, Closure... actions) {
    if (arguments instanceof Closure) {
      actions = [arguments, *actions] as Closure[]
      arguments = null
    }
    if (!actions) throw new IllegalArgumentException("One or more arguments of type groovy.lang.Closure required")
    _chain(arguments, actions.iterator())
  }

  /**
   * Implementation of the {@code chain} semantics in which there are multiple input arguments followed by the chain.
   * @param arguments All input arguments followed by the closure chain elements.
   */
  public static void chain(Object self, Object... arguments) {
    if (!arguments.any {
      it instanceof Closure
    }) throw new IllegalArgumentException("One or more arguments of type groovy.lang.Closure required")
    int i; def actions = []
    for (i = arguments.size() - 1; i >= 0; i--) {
      if (arguments[i] instanceof Closure) actions.add(0, arguments[i]) else break
    }
    _chain(arguments[0..i], actions.iterator())
  }

  private static void _chain(final Object arguments, final Iterator<Closure> actions) {
    if (actions) {
      def action = actions.next()
      if (arguments != null) {
        action = action.curry(arguments as Object[])
      }
      action.call { Object[] args -> _chain(args, actions) }
    }
  }

}