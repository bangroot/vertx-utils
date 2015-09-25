package com.github.bangroot.vertx

import spock.lang.*
import io.vertx.groovy.ext.unit.TestSuite

class BaseVertxSpecification extends Specification {

	@Shared TestSuite vertxSuite

	def setup() {
		//vertxSuite = TestSuite.create(getCurrentName())
	}

	protected String getCurrentName() {
		return specificationContext.getIterationInfo().getName()
	}
}
