package myapp.sharding

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import myapp.sharding.MyEntity.GetReply

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object MyShardingApp2 {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("ClusterSystem")

    ClusterSharding(system).start(
      typeName = MyEntity.shardName,
      entityProps = MyEntity.props,
      settings = ClusterShardingSettings(system),
      extractEntityId = MyEntity.idExtractor,
      extractShardId = MyEntity.shardResolver)
  }

}

