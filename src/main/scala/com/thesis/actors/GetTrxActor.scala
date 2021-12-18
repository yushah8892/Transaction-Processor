package com.thesis.actors

import akka.actor.Props
import com.thesis.actors.AddTrxToPool.AddTransaction
import com.thesis.actors.GetTrxActor.{GetAllTransactions, GetTransaction}
import com.thesis.core.{EnrichedTransaction, Transaction}
import com.thesis.crypto.HexString
import com.thesis.persist.persistTransaction

object  GetTrxActor {
  final case class GetTransaction(id: HexString)
  final case class GetAllTransactions()
  def props: Props = Props[GetTrxActor]

}
class GetTrxActor extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)


  override def receive: Receive = {
    case GetTransaction(id) => onGetTransaction(id)
    case GetAllTransactions() => onGetAllTransactions()
  }

  private def onGetTransaction(id: HexString): Unit = {
    log.info(s"${id} is process by ${self.toString()}")
    sender() ! persistTransaction.onGetTransactionFromRedis(id)
  }

  private def onGetAllTransactions():Unit = {
    log.info(s"ALl Trx is process by ${self.toString()}")
    sender() ! persistTransaction.onGetAllTransactions()
  }



}
