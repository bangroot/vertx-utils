vertx-utils
===========

This is a library of handy groovy extensions, mixins and utilities that make my life easier when writing Vert.x
projects. The extensions are self-registering via the [Groovy extension module](http://groovy.codehaus.org/Creating+an+extension+module)
mechanism. Mixins are optional functionality that can be mixed into Verticles using the [@Mixin annotation or 
use()](http://groovy.codehaus.org/Category+and+Mixin+transformations) methods.

Table of Contents
-----------
Extensions | Mixins
-------- | -------
[Chain](#chain)  | [HttpMixin](#httpmixin)
[Loop](#loop)   |
[Deploy](#deploy) |
[Replies](#replies) |

Extensions
-----------

###Chain

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

###Loop

_Inspired by work from Sascha Klein ([slides](http://www.slideshare.net/sascha_klein/vertx-using-groovy),
 [youtube](https://www.youtube.com/watch?v=dsRYKgNz55o&list=FLZcybLPmV_qCz2XObpUPjMg))_
 
The loop extension is applied to almost anything iterable and provides a way of looping over arrays but being in greater
control of when the next item is executed. Why do you need this instead of `.each`? Well, `.each` will move forward
in the iterator once control is returned to the thread. So, if in the processing of the item you call another service
the iterator will start executing the next item. But, if you need strict control, for either performance or logical
reasons, then loop is for you.

`loop` works much like each except that your closure gets a `next` parameter (a Closure) to call when you are ready 
to proceed.

```groovy
[1,2,3].loop {element, next -> println element; next();}
```

You can also pass an optional Closure to be called after processing is complete.

```groovy
[1,2,3].loop {element, next -> println element; next();}{println "done!"}
```

For Maps, the key and value are broken out

```groovy
[a:1,b:2,c:3].loop {key, value, next -> println "${key} = ${value}"; next();}
```

###Deploy

Deploy creates a small groovy DSL for deploying verticles. I prefer the simple, concise notation.

The only thing required to deploy a verticle is the identifier

```groovy
deploy {
  verticle "groovy:org.example.MyVerticle"
  //or worker
  worker "groovy:org.example.MyWorker"
}
```

You can also pass the configuration to be passed into the Verticle on start

```groovy
deploy {
  verticle "groovy:org.example.MyVerticle", [port: 8080]
}
```

As well as other DeploymentOptions

```groovy
deploy {
  verticle "groovy:org.example.MyVerticle", [:], [instances: 3]
}
```

###Replies

There seem to be a few standards growing out of the Vert.x community around message patterns. I've attempted
to codify them into helper functions added to Message objects.

`replyOkay` will send a reply to the message with `[status: 'ok']` as the body in addition to any other body elements
you pass in.

```groovy
  m.replyOkay()
  
  \\or
  
  m.replyOkay([some_return_value: foo])
```

`replyDenied` will reply with a `[status: 'denied']`

`replyError` will reply with a `[status: 'error']` plus any optional `message` supplied

Traits
---------

###HttpClientSupport

Should you require HttpClient support in your GroovyVerticle the HttpMixin provides a DSL for constructing more
complex calls. In addtion, it caches client instances and will even handle some session management when you
are interacting with a web site that uses session cookies.

To enable this support, simplify mix it into your Verticle

```groovy
@Mixin([HttpMixin])
class MyVerticle extends GroovyVerticle {
}
```

The syntax for the http dsl is `http url, closure` and the most basic example looks like

```groovy
http 'http://www.google.com', {
  GET()
}
```

This will perform an `HTTP GET` to www.google.com with no handler for a body or response ... not much help. 

####Responses
You can do a simple response handler

```groovy
http 'http://www.google.com', {
  responseHandler {response ->
  }
  GET()
}
```

Body handlers can come in several ways:

As a string
```groovy
http 'http://www.google.com', {
  withBody { body ->
    //body is a string
  }
  
  GET()
}
```

As a map (from JSON)
```groovy
http 'http://www.google.com', {
  withJsonBody { json ->
  }
  
  GET()
}
```

####HTTP Headers

Any unmatched method call in the closure is treated as an HTTP Header

```groovy
http 'http://www.google.com', {
  Content-Type 'application/json'
  GET()
}
```

####HTTP Methods

In addition to `GET()`, `POST()`, `PUT()`, `DELETE()`, `OPTIONS()` and `HEAD()` are supported.
 
####Request Body
Currently, only JSON bodies are supported. Provide a map as the body and the Http mixin will include it in the
request.

```groovy
http 'http://www.google.com', {
  body [foo:bar]
  POST()
}
```
