package net.randallalexander.restaurant.chooser.errors

import io.finch.Error
import io.finch.{Output, _}

object ErrorHandler {
  def apiErrorHandler: PartialFunction[Throwable, Output[Nothing]] = {
    case e: Error.NotPresent => BadRequest(e)
    case e: Error.NotParsed => BadRequest(e)
    case e: Error.NotValid => BadRequest(e)
    case e: Exception => InternalServerError(e)
  }
}
