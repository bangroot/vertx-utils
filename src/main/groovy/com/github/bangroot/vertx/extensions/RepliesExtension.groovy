package com.github.bangroot.vertx.extensions

import io.vertx.groovy.core.eventbus.Message

/**
 * Created by bangroot on 9/10/14.
 */
class RepliesExtension {

  public static void replyOkay(Message self, body = null) {
    def bodyToSend = (body) ?: [:]
    bodyToSend['status'] = 'ok'
    self.reply(bodyToSend)
  }

  public static void replyDenied(Message self) {
    self.reply([status: 'denied'])
  }

  public static void replyError(Message self, errorMsg = null) {
    def bodyToSend = [status: 'error', message: errorMsg]
    self.reply(bodyToSend)
  }
}
