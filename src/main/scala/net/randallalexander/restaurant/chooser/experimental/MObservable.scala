package net.randallalexander.restaurant.chooser.experimental

import com.twitter.util.Future
import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Observable, Observer, OverflowStrategy}
import net.randallalexander.restaurant.chooser.utils.FutureConversion._
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory

object MObservable {
  def observableTestRunner(emit: Seq[String]): Unit = {
    val logger = LoggerFactory.getLogger(this.getClass)
    def observableAction(id: Int)(value: String) = {
      logger.info(s"Observable::$id::$value")
      Future {
        Continue
      }.asScala
    }

    val observable = Observable.fromIterable(emit).asyncBoundary(OverflowStrategy.BackPressure(10))
    val rPublisher = observable.toReactivePublisher

    List(
      observable.subscribe(new DumpObserver),
      observable.subscribe(new DumpObserver),
      observable.subscribe(observableAction(0)(_)),
      observable.subscribe(observableAction(1)(_)))
    rPublisher.subscribe(subscriber(0))
    rPublisher.subscribe(subscriber(1))
  }

  private def subscriber(id: Int) = new org.reactivestreams.Subscriber[String] {
    val logger = LoggerFactory.getLogger(this.getClass)
    var subscription = Option.empty[Subscription]

    override def onError(t: Throwable): Unit = {
      logger.error("onError", t)
      subscription.map(_.cancel())
    }

    override def onSubscribe(s: Subscription): Unit = {
      logger.info("onSubscribe")
      subscription = Some(s)
      s.request(1)
    }

    override def onComplete(): Unit = {
      logger.info("onComplete")
      subscription.map(_.cancel())
    }

    override def onNext(value: String): Unit = {
      logger.info(s"Reactive::OnNext::$id::$value")
      subscription.map(_.request(1))
    }
  }

  private class DumpObserver[-A] extends Observer.Sync[A] {
    val logger = LoggerFactory.getLogger(this.getClass)

    def onNext(elem: A): Ack = {
      logger.info(s"Dump-->$elem")
      Continue
    }

    def onError(ex: Throwable) = {
      logger.info(s"Dump-->$ex")
    }

    def onComplete() = {
      logger.info(s"Dump completed")
    }
  }
}
