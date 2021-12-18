package com.thesis.persist

import com.redis._
import com.thesis.core.{EnrichedTransaction, Transaction}
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import org.slf4j.LoggerFactory

import java.time.LocalDateTime

object persistTransaction {


  val config = ConfigFactory.load()
  val logger = LoggerFactory.getLogger(this.getClass)

  val REDIS_CLIENT_IP = config.getString("config.REDIS_CLIENT_IP")
  val REDIS_CLIENT_PORT = config.getInt("config.REDIS_CLIENT_PORT")
  val REDIS_SECRET = config.getString("config.REDIS_SECRET")
  var redisClient: RedisClient = null

  try {
    redisClient = new RedisClient(REDIS_CLIENT_IP, REDIS_CLIENT_PORT, secret = Option(REDIS_SECRET))
  } catch {
    case e: Exception => logger.error(e.getMessage)
  }


  def onSaveTransactionToRedis(key: String, data: String): Boolean = {
    logger.info(s"save trx to redis initiated.${LocalDateTime.now()}")
    redisClient.set(key, data)
    logger.info(s"save trx to redis finished.${LocalDateTime.now()}")
    true
  }

  def onGetTransactionFromRedis(key: String): String = {
    implicit val formats = DefaultFormats
    logger.info(s"Read trx from redis initiated.${LocalDateTime.now()}")
    val trx = redisClient.get(key)
    logger.info(s"Read trx from redis finished.${LocalDateTime.now()}")

    if (trx.isDefined) {
      val trxObj = parse(trx.get).extract[EnrichedTransaction]
      prettyRender(Extraction.decompose(trxObj))
    } else {
      prettyRender(Extraction.decompose(None))
    }

  }

  def onGetAllTransactions(): String = {

    implicit val formats = DefaultFormats
    logger.info(s"Read All trx from redis initiated.${LocalDateTime.now()}")
    val keys = redisClient.keys("*").get
    logger.info(s"Read All trx from redis finished.${LocalDateTime.now()}")

    val values = keys.map(k => redisClient.get(k.get))
    val transactions = values.map(item => parse(item.get).extract[EnrichedTransaction])
    prettyRender(Extraction.decompose(transactions))
  }
}
