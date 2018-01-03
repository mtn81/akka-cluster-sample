package com.lightbend.akka.sample

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

object Sample extends App {
  implicit val timeout: Timeout = 5 seconds

  val system = ActorSystem("MySystem")
  import system.dispatcher

  val hoge = system.actorOf(Props[Hoge])

  (hoge ? "hoge").foreach(println)

}

class Hoge extends Actor {
  override def receive = {
    case "hoge" =>
      self ! ("foo", sender)
    case ("foo", receiver: ActorRef) =>
      receiver ! 1

  }
}
