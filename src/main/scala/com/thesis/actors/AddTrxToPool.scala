package com.thesis.actors

import akka.actor.Props
import com.thesis.actors.AddTrxToPool.AddTransaction
import com.thesis.core.Transaction
import com.thesis.persist.persistTransaction


object AddTrxToPool{
  final case class AddTransaction(tx: Transaction)
  def props: Props = Props[AddTrxToPool]

}

class AddTrxToPool extends ActorSupport {
  override def preStart(): Unit = log.info("{} started!", this.getClass.getSimpleName)
  override def postStop(): Unit = log.info("{} stopped!", this.getClass.getSimpleName)


  override def receive: Receive = {
    case AddTransaction(tx) => onAddTransaction(tx)

  }

  private def onAddTransaction(tx: Transaction): Unit = {

    val trx = persistTransaction.onGetTransactionFromRedis(tx.rawTransaction.seqId.toString + ":" +  tx.rawTransaction.clientId)
    if(!trx.isEmpty){
      sender() ! s"Transaction ${tx.rawTransaction.seqId} already exists in the Pool."
    }else{
      persistTransaction.onSaveTransactionToRedis(tx.rawTransaction.seqId.toString + ":" +  tx.rawTransaction.clientId,tx.toString)
      sender() ! s"Transaction ${tx.rawTransaction.seqId} created in the Pool."
    }

  }

}
