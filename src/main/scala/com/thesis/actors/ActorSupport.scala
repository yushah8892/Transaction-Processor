package com.thesis.actors

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt


trait ActorSupport extends Actor with ActorLogging  {

  // Required by the `ask` (?) method
  // usually we'd obtain the timeout from the system's configuration
  implicit lazy val timeout: Timeout = Timeout(5.seconds)

}
