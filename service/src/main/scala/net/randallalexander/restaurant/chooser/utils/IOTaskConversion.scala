package net.randallalexander.restaurant.chooser.utils

import cats.effect.IO
import com.twitter.util.{Future => TFuture, Promise => TPromise}


object IOTaskConversion {
  implicit class RichTask[A](t: => IO[A]) {
    def asTFuture:TFuture[A] = {
      val p: TPromise[A] = TPromise()
      t.unsafeRunAsync{
        cb =>
          cb match {
            case Right(result) => p.setValue(result)
            case Left(ex) => p.setException(ex)
          }
      }
      p
    }
  }


  implicit class RichTFuture[A](f: => TFuture[A]) {
    def asIO: IO[A] = {
      IO.async {
        cb =>
          f.onSuccess {
            result =>
              cb(Right(result))
          }
          f.onFailure {
            ex =>
              cb(Left(ex))
          }
          ()
      }
    }
  }
}
