package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie.implicits._

object PostgreSQL {
  def initDatabase: IO[Int] = (
    RestaurantDDL.initDatabaseQuery *>
      PersonDDL.initDatabaseQuery *>
      PreferenceDDL.initDatabaseQuery *>
      TransactionDDL.initDatabaseQuery
    ).transact(xa)
}
