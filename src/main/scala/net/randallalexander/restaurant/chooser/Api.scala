package net.randallalexander.restaurant.chooser

import com.twitter.finagle.Service
import com.twitter.finagle.http.filter.ExceptionFilter
import com.twitter.finagle.http.{Request, Response}
import io.finch._
import io.finch.circe._
import net.randallalexander.restaurant.chooser.errors.ErrorHandler
import net.randallalexander.restaurant.chooser.model.Hello

object Api {
  def helloApi() = hello :+: getEcho

  def hello: Endpoint[Hello] =
    get("v1" :: "hello" :: string("from") :: params("to")) { (from:String, to:Seq[String]) =>
      Ok(Hello(from, to))
    }

  def getEcho: Endpoint[String] =
    get("v1" :: "echo" :: string("what")) { (what: String) =>
      Ok(what)
    }

  private def api = helloApi()

  def apiService: Service[Request, Response] =
    ExceptionFilter andThen
      api.handle(ErrorHandler.apiErrorHandler).toService
}