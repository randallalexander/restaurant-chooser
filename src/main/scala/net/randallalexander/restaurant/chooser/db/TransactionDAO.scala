package net.randallalexander.restaurant.chooser.db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import java.util.UUID
import net.randallalexander.restaurant.chooser.model._


object TransactionDAO {

  def createTransaction(transaction: Transaction): IO[Transaction] = {
    val id = UUID.randomUUID().toString
    val transactionWithId = transaction.copy(id=Some(id))
    createTransactionQuery(transactionWithId).run.transact(xa).map(_ => transactionWithId)
  }

  private def createTransactionQuery(transaction: Transaction):Update0 = {
    sql"""INSERT INTO transactions (id, restaurant_id, total, number_of_people) VALUES (${transaction.id}, ${transaction.restaurantId}, ${transaction.total}, ${transaction.numberOfPeople})""".update
  }

  def deleteTransaction(id:String): IO[Int] = {
    deleteTransactionQuery(id).run.transact(xa)
  }

  private def deleteTransactionQuery(transactionId: String):Update0 = {
    sql"""DELETE FROM transactions WHERE id = $transactionId""".update
  }

  def getTransaction(id:String): IO[Option[Transaction]] = {
    getTransactionQuery(id).transact(xa)
  }

  private def getTransactionQuery(transactionId:String):ConnectionIO[Option[Transaction]] = {
    sql"""SELECT id, restaurant_id, total, number_of_people FROM transactions WHERE id = $transactionId""".query[Transaction].option
  }
}

object TransactionDDL {
  def initDatabaseQuery = (
      dropTransactions.run *>

      createTransactions.run
    )

  private val dropTransactions:Update0 =
    sql"""
    DROP TABLE IF EXISTS transactions CASCADE
  """.update

  private val createTransactions:Update0 =
    sql"""
    CREATE TABLE transactions (
      id VARCHAR NOT NULL PRIMARY KEY,
      restaurant_id VARCHAR NOT NULL REFERENCES restaurant(id),
      total NUMERIC(8, 2),
      number_of_people INTEGER
    )
  """.update
}