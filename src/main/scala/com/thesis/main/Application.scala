package com.thesis.main

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.thesis._
import com.thesis.actors.{AddTrxToPool, GetTrxActor}
import akka.pattern.ask
import akka.util.Timeout
import com.thesis.actors.GetTrxActor.GetTransaction
import com.thesis.core.Transaction

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{ Failure, Success }


object Application {
  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  implicit val system: ActorSystem = ActorSystem("aplos-service")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val addTrxActor: ActorRef = system.actorOf(AddTrxToPool.props, ADD_TRX_ACTOR_NAME)
  val getTrxActor: ActorRef = system.actorOf(GetTrxActor.props, GET_TRX_ACTOR_NAME)


  lazy val log = Logging(system, classOf[App])
  val result = getTrxActor ? GetTransaction("32")

  result.onComplete{
    case Success(value) => print(value)
    case Failure(exception) => sys.error(exception.getLocalizedMessage)
  }
  //val result = Await.result(message,timeout.duration)
  Await.result(system.whenTerminated, Duration.Inf)


}
