package net.randallalexander.restaurant.chooser

import java.util.UUID

import com.twitter.finagle.Http
import com.twitter.util.Await

object Boot extends App {
  Await.ready(Http.server.withLabel(s"restaurant-chooser-${UUID.randomUUID()}").serve(":8081", Api.apiService))
}