package net.randallalexander.restaurant.chooser
/*
import java.util.concurrent.TimeUnit

import cats.free.Free
import com.zaxxer.hikari.HikariDataSource
import doobie.free.connection
import doobie.hikari.hikaritransactor.HikariTransactor
import doobie.imports._
import fs2.Task

import scala.concurrent.duration.{Duration, FiniteDuration}
//import fs2.interop.cats._

//https://tpolecat.github.io/doobie-0.2.1/13-Extensions-PostgreSQL.html
//import doobie.contrib.postgresql.pgtypes._

package object db {


  //"org.tpolecat"       %% "doobie-hikari-cats" % "0.4.1",

  val transactor: HikariTransactor[Task] = HikariTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:resturaunt", "postgres", "postgress%FOO"
  ).unsafeRunFor(Duration(5, TimeUnit.SECONDS))

  sys.addShutdownHook(transactor.shutdown.unsafeRunFor(Duration(10, TimeUnit.SECONDS)))

  /*
  Manually setup the HikariDataSource:
  val ds = new HikariDataSource()
  ds.setDriverClassName("org.postgresql.Driver")
  ds.setJdbcUrl("jdbc:postgresql:resturaunt")
  ds.setUsername("postgres")
  ds.setPassword("postgress%FOO")

  val hikariTrans = HikariTransactor[fs2.Task](ds)
  def transact[A](q: Free[connection.ConnectionOp, A]): fs2.Task[A] = {
    q.transact(hikariTrans)
  }
  */

}
*/