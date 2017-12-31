package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}

object ParentChildExample extends App {

  class Parent extends Actor with ActorLogging {

    override def supervisorStrategy: SupervisorStrategy =
      OneForOneStrategy()({
        case _ => {
          println(s"error in $sender")
          SupervisorStrategy.Restart
        }
      })

    override def receive: Receive = {
      case "error" => {
        val child = context.actorOf(Props[Child], "Child1")
        child ! "error"
      }
    }
  }

  class Child extends Actor with ActorLogging {

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      println("restart")
      super.preRestart(reason, message)
    }
    override def preStart(): Unit = {
      println("child started")
    }
    override def postStop(): Unit =
      println("child stopped")

    override def receive: Receive = {
      case "error" => throw new Exception("test error")
    }
  }

  val system = ActorSystem("test")
  val parent = system.actorOf(Props[Parent], "Parent1")
  parent ! "error"

}
