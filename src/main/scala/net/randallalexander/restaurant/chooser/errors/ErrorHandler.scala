package net.randallalexander.restaurant.chooser.errors

import io.finch.Error
import io.finch.{Output, _}
import journal.Logger

object ErrorHandler {
  val logger = Logger[ErrorHandler.type]

  def apiErrorHandler: PartialFunction[Throwable, Output[Nothing]] = {
    case e: Error.NotPresent =>
      logger.info(s"Missing request item: ${e.getMessage()}",e)
      BadRequest(e)
    case e: Error.NotParsed =>
      logger.info(s"Can not parse request: ${e.getMessage()}",e)
      BadRequest(e)
    case e: Error.NotValid =>
      logger.info(s"Finch validation error: ${e.getMessage()}",e)
      BadRequest(e)
    case e: Exception =>
      logger.error(s"General error: ${e.getMessage()}",e)
      InternalServerError(e)
  }
}

object Errors {
  case class ConstraintViolation(message:String)
}
