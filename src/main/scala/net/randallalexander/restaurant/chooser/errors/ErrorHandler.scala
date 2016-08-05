package net.randallalexander.restaurant.chooser.errors

import io.finch.Error.{NotParsed, NotPresent, NotValid, RequestErrors}
import io.finch.{Output, _}

object ErrorHandler {
  def apiErrorHandler: PartialFunction[Throwable, Output[Nothing]] = {
    case e: NotPresent => BadRequest(e)
    case e: NotParsed => BadRequest(e)
    case e: NotValid => BadRequest(e)
    case e: RequestErrors => BadRequest(e)
    case e: Exception => InternalServerError(e)
  }
}
