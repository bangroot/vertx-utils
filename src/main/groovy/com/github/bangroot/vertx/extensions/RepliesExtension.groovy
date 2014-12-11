package com.github.bangroot.vertx.extensions

import org.vertx.groovy.core.eventbus.Message

/**
 * Created by bangroot on 9/10/14.
 */
class RepliesExtension {

  public static void sendOkay(Message self, body = null) {
    def bodyToSend = (body) ?: [:]
    bodyToSend['status'] = 'ok'
    self.reply(bodyToSend)
  }

  public static void sendDenied(Message self) {
    self.reply([status: 'denied'])
  }

  public static void sendError(Message self, errorMsg = null) {
    def bodyToSend = [status: 'error', message: errorMsg]
    self.reply(bodyToSend)
  }
}
