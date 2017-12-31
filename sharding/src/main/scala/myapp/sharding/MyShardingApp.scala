package myapp.sharding

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import myapp.sharding.MyEntity.GetReply

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object MyShardingApp {

  def main(args: Array[String]): Unit = {

    Seq(2551, 2552).foreach { port =>
      val config = ConfigFactory.parseString(
        s"""
          akka.remote.netty.tcp.port = $port
          akka.cluster.roles = [server]
        """).
        withFallback(ConfigFactory.load())

      val system = ActorSystem("ClusterSystem", config)

      ClusterSharding(system).start(
        typeName = MyEntity.shardName,
        entityProps = MyEntity.props,
        settings = ClusterShardingSettings(system),
        extractEntityId = MyEntity.idExtractor,
        extractShardId = MyEntity.shardResolver)
    }

  }

}

object MyShardClientApp {
  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.parseString(
      s"""
          akka.remote.netty.tcp.port = 2553
          akka.cluster.roles = [client]
        """).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)

    ClusterSharding(system).startProxy(
      typeName = MyEntity.shardName,
      role = Some("server"),
      extractEntityId = MyEntity.idExtractor,
      extractShardId = MyEntity.shardResolver)

    val myEntityRegion: ActorRef = ClusterSharding(system).shardRegion(MyEntity.shardName)
    implicit val timeout: Timeout = 3 seconds
    implicit val ec: ExecutionContext = system.dispatcher

    for {
      r11 <- (myEntityRegion ? MyEntity.Get(1)).mapTo[GetReply]
      _ <- (myEntityRegion ? MyEntity.Update(1))
      _ <- (myEntityRegion ? MyEntity.Update(1))
      r12 <- (myEntityRegion ? MyEntity.Get(1)).mapTo[GetReply]
      r21 <- (myEntityRegion ? MyEntity.Get(2)).mapTo[GetReply]
      _ <- (myEntityRegion ? MyEntity.Update(2))
      r22 <- (myEntityRegion ? MyEntity.Get(2)).mapTo[GetReply]
    } yield {
      println(r11, r12)
      println(r21, r22)
    }
  }
}
