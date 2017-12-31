package myapp.sharding

import akka.actor.{Actor, Props}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.HashCodeMessageExtractor

class MyEntity extends Actor {
  import MyEntity._

  var status: Int = 0

  override def receive: Receive = {
    case Get(id) =>
      println(s"process Get in $self")
      sender() ! GetReply(id, status)

    case Update(id) =>
      println(s"process Update in $self")
      status += 1
      sender() ! UpdateReply(id)
  }
}


object MyEntity {

  case class Update(id: Long)
  case class UpdateReply(id: Long)

  case class Get(id: Long)
  case class GetReply(id: Long, status: Int)

  private val msgExtractor = new HashCodeMessageExtractor(100) {
    override def entityId(message: Any): String = message match {
      case m @ Get(id) => id.toString
      case m @ Update(id) => id.toString
    }
  }

  val idExtractor: ShardRegion.ExtractEntityId = {
    case m: Any =>
      (msgExtractor.entityId(m), msgExtractor.entityMessage(m))
  }

  val shardResolver: ShardRegion.ExtractShardId =
    msgExtractor.shardId

  val shardName: String = "MyEntity"

  def props: Props = Props(new MyEntity)
}
