package com.github.bangroot.vertx.traits.data;

import spock.lang.*;

public class QueryParserSpec extends Specification {

	@Unroll
	def "parsing find #name into #query"(def name, def query) {

		given:
			QueryParser parser = new QueryParser()

		when:
			def output = parser.parse(name)

		then:
			assert output.equals(query)

		where:

			name | query

			"findPerson" | "SELECT * FROM person;"
			"findPersonWhereNameEquals" | "SELECT * FROM person WHERE name = ?;"
			"findPurplePerson" | "SELECT * FROM purple_person;"
			"findPersonWhereNameEqualsAndAgeEquals" | "SELECT * FROM person WHERE name = ? AND age = ?;"
	}

	@Unroll
	def "parsing insert #name with #props into #query"(def name, def props, def query) {
		given:
			QueryParser parser = new QueryParser()

		when:
			def output = parser.parse(name, props)

		then:
			assert output.equals(query)

		where:
			
			name | props | query
			"insertPerson" | [name: "Bob", age: 32] | "INSERT INTO person (age, name) VALUES (?, ?);"
			"insertPurplePerson" | [name: "Bob", age: 32] | "INSERT INTO purple_person (age, name) VALUES (?, ?);"
			"insertPurplePerson" | [firstName: "Bob", lastAge: 32] | "INSERT INTO purple_person (first_name, last_age) VALUES (?, ?);"
	}
	
	@Unroll
	def "parsing update #name with #props into #query"(def name, def props, def query) {
		given:
			QueryParser parser = new QueryParser()

		when:
			def output = parser.parse(name, props)

		then:
			assert output.equals(query)

		where:
			
			name | props | query
			"updatePerson" | [id: 32, name: "Bob", age: 32] | "UPDATE person SET age = ?, name = ? WHERE id = ?;"
			"updatePurplePerson" | [id: 32, name: "Bob", age: 32] | "UPDATE purple_person SET age = ?, name = ? WHERE id = ?;"
			"updatePurplePerson" | [id: 32, firstName: "Bob", lastAge: 32] | "UPDATE purple_person SET first_name = ?, last_age = ? WHERE id = ?;"
	}
	
	@Unroll
	def "parsing upsert #name with #props into #query"(def name, def props, def query) {
		given:
			QueryParser parser = new QueryParser()

		when:
			def output = parser.parse(name, props)

		then:
			assert output.equals(query)

		where:
			
			name | props | query
			"upsertPerson" | [id: 32, name: "Bob", age: 32] | "UPDATE person SET age = ?, name = ? WHERE id = ?;"
			"upsertPerson" | [name: "Bob", age: 32] | "INSERT INTO person (age, name) VALUES (?, ?);"
	}
	
	@Unroll
	def "parsing delete #name with #props into #query"(def name, def props, def query) {
		given:
			QueryParser parser = new QueryParser()

		when:
			def output = parser.parse(name, props)

		then:
			assert output.equals(query)

		where:
			
			name | props | query
			"deletePerson" | [id: 32, name: "Bob", age: 32] | "DELETE FROM person WHERE id = ?;"
			"deletePurplePerson" | [id: 32, name: "Bob", age: 32] | "DELETE FROM purple_person WHERE id = ?;"
	}
}
