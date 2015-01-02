vertx-utils
===========

This is a library of handy groovy extensions, mixins and utilities that make my life easier when writing Vert.x
projects. The extensions are self-registering via the [Groovy extension module](http://groovy.codehaus.org/Creating+an+extension+module)
mechanism. Mixins are optional functionality that can be mixed into Verticles using the [@Mixin annotation or 
use()](http://groovy.codehaus.org/Category+and+Mixin+transformations) methods.

Extensions
-----------

**Chain**

_Inspired by work from Sascha Klein ([slides](http://www.slideshare.net/sascha_klein/vertx-using-groovy),
 [youtube](https://www.youtube.com/watch?v=dsRYKgNz55o&list=FLZcybLPmV_qCz2XObpUPjMg))_
 
Chain allows you to more easily link multiple steps in a chain of events together, helping to eliminate the nested,
triangular look of more complex operations involving lots of reply handlers. While the services model in vertx 3
helps eliminate this somewhat, this pattern still has enormous value. The most basic chain is a collection of
closures which take, at least, one parameter, the next closure to call.

```groovy
chain {next -> do_something(); next() }, { next -> something_else(); message.reply(); next() }
```
    
The chain extension will invoke the first closure passing in a the next closure to be called as the final parameter.
If the closure is the last in the chain, next will still be passed as an empty closure. This allows you to create
more reusable code and safely call `next()` even if there is not another step in the chain. Of course, to make things
more resuable, you'd want to declare the closures as variables and reference them in the chain:

```groovy

def first_step = { next -> 
  do_something();
  next();
}

def next_step = { next ->
  something_else();
  message.reply();
  next();
}

chain first_step, next_step
```

Inputs can be passed into the first closure using an array of inputs

```groovy
chain 10, 20, 
  {input_10, input_20, next -> print_int(input_10); next() }, 
  { next -> something_else(); message.reply(); next()}
```
    
You can pass inputs into the next call as well.

```groovy
chain 10, 20, 
  {input_10, input_20, next -> print_int(input_10); next(input_20) }, 
  { input, next -> print_int(input); message.reply(); next()}
```

If you prefer to think in more structured classes and methods instead of a bunch of closure variables, consider
using the `.&` groovy notation.

```groovy
class MyVerticle extends GroovyVerticle {
  def start() {
    vertx.eventBus.messageConsumer('login', this.&loginUser)
  }

  def loginUser(Message m) {
    chain m, this.&verifyUser, this.&createSession
  }
  
  def verifyUser(Message m, Closure next) {
    if (m.body.password == 'goodpassword') {
      next(m)
    } else {
      m.reply([status: 'access_denied'])
    }
  }
  
  def createSession(Message m, Closure next) {
    //do something to create a session
    m.reply([status: 'ok'])
  }
}
```    
