package com.thesis.core

import com.thesis.crypto.SHA256
import com.thesis.formats
import net.liftweb.json.JsonAST.RenderSettings.compact
import net.liftweb.json.JsonAST.render
import net.liftweb.json.{Extraction, JValue, prettyRender}

import java.time.LocalDateTime
import java.util.UUID

case class Transaction(val rawTransaction:Parameters){
  val trxId = UUID.randomUUID()
  val timeStamp = LocalDateTime.now().getNano
  val hash = hashOfTransaction(this)

  def hashOfTransaction(tx: Transaction): String = SHA256.hashStrings(
    rawTransaction.clientId,rawTransaction.seqId.toString,tx.timeStamp.toString,tx.timeStamp.toString,rawTransaction.learnings)

  def toJson: JValue = Extraction.decompose(EnrichedTransaction(rawTransaction.seqId,rawTransaction.learnings,rawTransaction.clientId,hash,trxId.toString,timeStamp))

  override def toString: String = prettyRender(toJson)
}

case class EnrichedTransaction( seqId :Double,learnings : String,clientId:String,
                        hash:String,trxId:String,timeStamp:Double)


case class Parameters(
                       seqId: Double,
                       learnings: String,
                       clientId: String
                     )
case class TrxInputMessage(
                  method: String,
                  responseKafkaTopic: String,
                  parameters: Parameters
                )
case class TrxRootMessage(
                           input: TrxInputMessage
                         )