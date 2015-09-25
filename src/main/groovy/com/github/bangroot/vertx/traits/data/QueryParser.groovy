package com.github.bangroot.vertx.traits.data;

import java.util.LinkedList
import java.util.Queue
import java.util.Stack

public class QueryParser {

	private static final PATTERN_SPLIT_CAMEL = '(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])'
	def object

	public String parse(String input, def object = null) {
		this.object = object as TreeMap
		def inputParts = input.split(PATTERN_SPLIT_CAMEL) as LinkedList
		def query = new StringBuilder();
		def handlers = [new InitialHandler()] as Stack

		while(!handlers.empty()) {
			def currentHandler = handlers.peek()
			currentHandler.handle(handlers, inputParts, query);
			if (currentHandler == handlers.peek()) {
				currentHandler = handlers.pop()
				currentHandler.exit(query)
			}
		}

		return query.toString();
	}

	private abstract class Handler {
		def handlers = [
			"Where": WhereHandler
		]

		def operators = [
			"Equals": "="
		]

		def booleans = [
			"And": "AND"
		]

		def visited = false

		def handle(Stack handlerStack, Queue inputParts, StringBuilder query) {
			if(!visited) { 
				visited = true
				enter(handlerStack, inputParts, query)
			}
		}
		
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {}

		def exit(StringBuilder query) {}
	}

	private class InitialHandler extends Handler {
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			def queryType = inputParts.peek();

			if (queryType.equals("upsert")) {
				queryType = (object.id) ? "update" : "insert"
			}

			switch (queryType) {
				case "find":
					handlerStack.push(new FindHandler())
					break
				case "insert":
					handlerStack.push(new InsertHandler())
					break
				case "update":
					handlerStack.push(new UpdateHandler())
					break
				case "delete":
					handlerStack.push(new DeleteHandler())
					break
			}

		}

		def exit(StringBuilder query) {
			query << ";"
		}
	}

	private class FindHandler extends Handler {
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			inputParts.poll()
			query << "SELECT * FROM ${inputParts.poll().toLowerCase()}"
			while(!inputParts.empty && !handlers.containsKey(inputParts.peek())) {
				query << "_${inputParts.poll().toLowerCase()}"
			}

			if(!inputParts.empty) { 
				handlerStack.push(handlers[inputParts.peek()].newInstance())
			}
		}
	}

	private class WhereHandler extends Handler {
		def joined = false

		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			inputParts.poll()
			if(!joined) { 
				query << " WHERE"
			}
			query << " ${inputParts.poll().toLowerCase()}"
			while(!inputParts.empty && !operators.containsKey(inputParts.peek())) {
				query << "_${inputParts.poll().toLowerCase()}"
			}
			if (inputParts.empty) {
				inputParts.add("Equals")
			}
			query << " ${operators[inputParts.poll()]} ?"

			if (booleans.containsKey(inputParts.peek())) {
				query << " ${booleans[inputParts.peek()]}"
				def handler = new WhereHandler()
				handler.joined = true
				handlerStack.push(handler)
			}
		}
	}

	private class InsertHandler extends Handler {
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			inputParts.poll()

			query << "INSERT INTO ${inputParts.poll().toLowerCase()}"
			while(!inputParts.empty && !handlers.containsKey(inputParts.peek())) {
				query << "_${inputParts.poll().toLowerCase()}"
			}

			query << " ("
			object.keySet().eachWithIndex { key, idx ->
				if (idx > 0) { query << ", " }
				def keyParts = key.split(PATTERN_SPLIT_CAMEL) as LinkedList
				query << "${keyParts.poll().toLowerCase()}"
				while(!keyParts.empty) {
					query << "_${keyParts.poll().toLowerCase()}"
				}
			}

			query << ") VALUES ("
			def paramList = new String[object.keySet().size()]
			Arrays.fill(paramList, "?")
			query << paramList.join(", ")
			query << ")"
		}
	}

	private class UpdateHandler extends Handler {
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			inputParts.poll()

			def trimmedObject = new TreeMap(object)
			trimmedObject.remove('id')

			query << "UPDATE ${inputParts.poll().toLowerCase()}"
			while(!inputParts.empty && !handlers.containsKey(inputParts.peek())) {
				query << "_${inputParts.poll().toLowerCase()}"
			}

			query << " SET "
			trimmedObject.keySet().eachWithIndex { key, idx ->
				if (idx > 0) { query << ", " }
				def keyParts = key.split(PATTERN_SPLIT_CAMEL) as LinkedList
				query << "${keyParts.poll().toLowerCase()}"
				while(!keyParts.empty) {
					query << "_${keyParts.poll().toLowerCase()}"
				}
				query << " = ?"
			}

			query << " WHERE id = ?"

		}
	}
	
	private class DeleteHandler extends Handler {
		def enter(Stack handlerStack, Queue inputParts, StringBuilder query) {
			inputParts.poll()

			query << "DELETE FROM ${inputParts.poll().toLowerCase()}"
			while(!inputParts.empty && !handlers.containsKey(inputParts.peek())) {
				query << "_${inputParts.poll().toLowerCase()}"
			}

			query << " WHERE id = ?"

		}
	}
}
