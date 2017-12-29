package net.randallalexander.restaurant.chooser.utils

import com.twitter.util.{Return, Throw, Future => TFuture, Promise => TPromise}
import scala.concurrent.{ExecutionContext, Future => SFuture, Promise => SPromise}
import scala.util.{Failure, Success}

/*
  Taken from:
    https://github.com/finagle/finch/blob/master/docs/cookbook.md#converting-between-scala-futures-and-twitter-futures
  Which I believe is from twitter bijection

  I have done minor modifications
 */
object FutureConversion {

  implicit class RichTFuture[A](f: => TFuture[A]) {
    def asScala: SFuture[A] = {
      val p: SPromise[A] = SPromise()
      f.respond {
        case Return(value) =>
          p.success(value)
          ()
        case Throw(exception) =>
          p.failure(exception)
          ()
      }

      p.future
    }
  }

  implicit class RichSFuture[A](f: => SFuture[A]) {
    def asTwitter(implicit e: ExecutionContext): TFuture[A] = {
      val p: TPromise[A] = new TPromise[A]
      f.onComplete {
        case Success(value) => p.setValue(value)
        case Failure(exception) => p.setException(exception)
      }

      p
    }
  }

}
