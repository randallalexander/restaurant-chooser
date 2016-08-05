package net.randallalexander.restaurant.chooser

import java.util.UUID

import com.twitter.finagle.param.Stats
import com.twitter.finagle.stats.LoadedStatsReceiver
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import org.slf4j.LoggerFactory

object Boot extends App{
  val logger = LoggerFactory.getLogger(this.getClass)

  lazy val server: ListeningServer = Http.server
    .withLabel(s"restaurant-chooser-${UUID.randomUUID()}")
    .configured(Stats(LoadedStatsReceiver))
    .serve("0.0.0.0:8080", Api.apiService)

  def boot(): Unit = {
    logger.info("Booting...")
    sys.addShutdownHook(shutdown())
    Await.ready(server)
  }

  private def shutdown(): Unit = {
    logger.info("Shutting down...")
    Await.ready(server.close())
  }

  boot()
}