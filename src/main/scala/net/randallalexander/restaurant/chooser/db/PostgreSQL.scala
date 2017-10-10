package net.randallalexander.restaurant.chooser.db

import cats.free.Free
import com.twitter.util.Future
import doobie.free.connection.ConnectionOp
import fs2.Task
import net.randallalexander.restaurant.chooser.db

object PostgreSQL {
  import doobie.imports._
  import cats._, cats.data._, cats.implicits._
  import fs2.interop.cats._

  import scala.concurrent.ExecutionContext.Implicits.global
  import net.randallalexander.restaurant.chooser.utils.FutureConversion._

  def initPersonTable: Future[Int] =
    transactor.trans(initPersonTableQuery).unsafeRunAsyncFuture().asTwitter

  private def initPersonTableQuery = (dropPerson.run *> createPerson.run)

  private val dropPerson:Update0 =
    sql"""
    DROP TABLE IF EXISTS person
  """.update

  private val createPerson:Update0 =
    sql"""
    CREATE TABLE person (
      id SERIAL,
      nickname varchar NOT NULL unique,
      fname varchar NOT NULL,
      lname varchar NOT NULL
    )
  """.update
}
