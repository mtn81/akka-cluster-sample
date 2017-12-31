package myapp.sharding

import akka.actor.{Actor, ActorRef}
import akka.cluster.sharding.ClusterSharding

class MyClient extends Actor {
  import MyClient._

  val myEntityRegion: ActorRef = ClusterSharding(context.system).shardRegion(MyEntity.shardName)


  override def receive: Receive = {
    case Get(id) =>
      myEntityRegion ! MyEntity.Get(id)
    case Execute(id) =>
      myEntityRegion ! MyEntity.Update(id)
  }
}


object MyClient {

  case class Get(id: Long)
  case class Execute(id: Long)

}
