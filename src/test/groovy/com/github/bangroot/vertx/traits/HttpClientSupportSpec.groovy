package com.github.bangroot.vertx.traits

import com.github.bangroot.vertx.BaseVertxSpecification

class HttpClientSupportSpec extends BaseVertxSpecification {

	def "test basic call"() {
		given:
			
		when:
			vertxSuite.test(getCurrentName()) { context ->
				def async = context.async()
				eventBus.consumer("the-address", { msg ->
					async.complete()
				})
			}

		then:
			vertxSuite.run([
				reporters:[
					[
						to:"console"
					]
				]
			])
	}
}
