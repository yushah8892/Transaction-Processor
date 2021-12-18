package com.thesis.consumer

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.thesis.actors.AddTrxToPool.AddTransaction
import com.thesis.{ADD_TRX_ACTOR_NAME, GET_TRX_ACTOR_NAME}
import com.thesis.actors.GetTrxActor.{GetAllTransactions, GetTransaction}
import com.thesis.actors.{AddTrxToPool, GetTrxActor}
import com.thesis.core.{Parameters, Transaction, TrxInputMessage, TrxRootMessage}
import com.typesafe.config.ConfigFactory
import net.liftweb.json.{DefaultFormats, Extraction, Formats, JsonAST, parse, prettyRender}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import java.util
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object consumeFromKafka extends  App{


  override def main(args: Array[String]): Unit = {

    implicit val formats = DefaultFormats
   // implicit val a: Formats[Parameters] =
    implicit lazy val timeout: Timeout = Timeout(5.seconds)

    implicit val system: ActorSystem = ActorSystem("aplos-service1")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val addTrxActor: ActorRef = system.actorOf(RoundRobinPool(2).props(Props[AddTrxToPool]),ADD_TRX_ACTOR_NAME)
    val getTrxActor: ActorRef = system.actorOf(RoundRobinPool(2).props(Props[GetTrxActor]), GET_TRX_ACTOR_NAME)

    //lazy val log = Logging(system, classOf[App])
    val logger = LoggerFactory.getLogger(this.getClass)

    val config = ConfigFactory.load()

    val KAFKA_BOOTSTRAP_SERVER = config.getString("config.KAFKA_BOOTSTRAP_SERVER")
    val KAFKA_BOOTSTRAP_SERVER_PORT = config.getString("config.KAFKA_BOOTSTRAP_SERVER_PORT")
    val KAFKA_SERVER_TOPIC = config.getString("config.KAFKA_SERVER_TOPIC")
    val KAFKA_CONSUMER_POLL_TIME_IN_MS = config.getInt("config.KAFKA_CONSUMER_POLL_TIME_IN_MS")

    logger.info(s"environment value ${config.getString("config.environment")}")

    val props = new Properties()
    props.put("bootstrap.servers", s"${KAFKA_BOOTSTRAP_SERVER}:${KAFKA_BOOTSTRAP_SERVER_PORT}")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "consumer-group")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(KAFKA_SERVER_TOPIC))
    while (true) {
      val record = consumer.poll(KAFKA_CONSUMER_POLL_TIME_IN_MS).asScala
      for (data <- record.iterator) {
        consumer.commitSync()

        val parsedMessage = parse(data.value())
        val method = (parsedMessage \ "input" \ "method").extract[String]
        val topic = (parsedMessage \ "input" \ "responseKafkaTopic").extract[String]
        logger.info(s"Received Message: ${method}")
          if(method.equals("GET_TRX")){
            val rawTrx = (parsedMessage \ "input" \ "parameters").extract[Parameters]
            logger.info(s"Parameters for ${method} is : ${prettyRender(Extraction.decompose(rawTrx))}")
            val result = getTrxActor ? GetTransaction(rawTrx.seqId + ":" + rawTrx.clientId )
            result.onComplete{
              case Success(value) => {
                logger.info(s"Message ${method} is successfully processed.${value.toString}")
                writeToKafka(topic,value.toString)
              }
              case Failure(exception) => sys.error(exception.getLocalizedMessage)
            }
          }else if(method.equals("ADD_TRX")){
            val rawTrx = (parsedMessage \ "input" \ "parameters").extract[Parameters]
            logger.info(s"Parameters for ${method} is : ${prettyRender(Extraction.decompose(rawTrx))}")
            val result = addTrxActor ? AddTransaction(new Transaction(rawTrx))
            result.onComplete{
              case Success(value) => {
                logger.info(s"Message ${method} is successfully processed.${value.toString}")
                writeToKafka(topic,value.toString)
              }
              case Failure(exception) => sys.error(exception.getLocalizedMessage)
            }
          }else if(method.equals("GET_ALL_TRX")){
            val result = getTrxActor ? GetAllTransactions()
            result.onComplete{
              case Success(value) => {
                logger.info(s"Message ${method} is successfully processed.${value.toString}")
                writeToKafka(topic,value.toString)
              }
              case Failure(exception) => sys.error(exception.getLocalizedMessage)
            }
          }


      }
    }


    def writeToKafka(topic: String,data:String): Unit = {
      val props = new Properties()
      props.put("bootstrap.servers", s"${KAFKA_BOOTSTRAP_SERVER}:${KAFKA_BOOTSTRAP_SERVER_PORT}")
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      val producer = new KafkaProducer[String, String](props)
      logger.info(s"Sending Reply to Kafka on topic ${topic}")
      val record = new ProducerRecord[String, String](topic, "1", data)
      producer.send(record)
      producer.close()
    }
  }

  }
