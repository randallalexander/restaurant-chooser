package net.randallalexander.restaurant.chooser

import java.util.UUID

import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import org.slf4j.LoggerFactory

object Boot extends TwitterServer{
  val logger = LoggerFactory.getLogger(this.getClass)

  val server: ListeningServer = Http.server
    .withLabel(s"restaurant-chooser-${UUID.randomUUID()}")
    .withStatsReceiver(statsReceiver)
    .serve("0.0.0.0:8080", Api.apiService)

  private def shutdown(): Unit = {
    logger.info("Shutting down...")
    Await.ready(server.close())
  }
  def main(): Unit = {
    logger.info("Booting...")
    onExit {server.close()}
    Await.ready(adminHttpServer)
  }
}