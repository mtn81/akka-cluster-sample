package com.lightbend.akka.sample

import akka.actor.Actor.emptyBehavior
import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorRef, ActorSystem, DeathPactException, OneForOneStrategy, PoisonPill, Props, Terminated}
import akka.util.Timeout
import com.lightbend.akka.sample.TerminationExample.Parent.{GetChildReply, GetChildReq}

import scala.concurrent.duration._

object TerminationExample {

  class Parent extends Actor {
    override def preStart(): Unit = println("Parent started")
    override def postStop(): Unit = println("Parent stopped")

    val c1 = context.actorOf(Props[Child])
    context.watch(c1)
    val c2 = context.actorOf(Props[Child])
    context.watch(c2)

    def receive: Receive = {
      case "stop child" =>
        context.stop(c1)
//      case Terminated(_) =>
//        println("terminated!!!")
    }
  }
  object Parent {
    case object GetChildReq
    case class GetChildReply(child: ActorRef)
  }

  class Child extends Actor {

    override def preStart(): Unit = println("Child started")
    override def postStop(): Unit = println("Child stopped")

    def receive: Receive = emptyBehavior
  }

  def main(args: Array[String]): Unit = {
    import akka.pattern._
    import scala.concurrent.duration._

    val system = ActorSystem("MySystem");
    implicit val ec = system.dispatcher
    implicit val timeout: Timeout = 3 seconds

    val parent = system.actorOf(Props[Parent])

    parent ! "stop child"

  }

}
