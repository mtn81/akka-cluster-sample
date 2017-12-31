package myapp

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import myapp.sharding.MyEntity
import myapp.sharding.MyEntity.{GetReply, UpdateReply}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object ApiServer {
  import akka.http.scaladsl.server.Directives._
  import akka.pattern._

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("ClusterSystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    ClusterSharding(system).startProxy(
      typeName = MyEntity.shardName,
      role = Some("server"),
      extractEntityId = MyEntity.idExtractor,
      extractShardId = MyEntity.shardResolver)

    val myEntities: ActorRef = ClusterSharding(system).shardRegion(MyEntity.shardName)
    implicit val timeout: Timeout = 3 seconds


    val route =
      path("update" / LongNumber) { id =>
        get {
          val reply: Future[UpdateReply] = (myEntities ? MyEntity.Update(id)).mapTo[UpdateReply]
          onComplete(reply) {
            case Success(r) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Update ${r.id}"))
            case Failure(e) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Err ${e}"))
          }
        }
      } ~
      path("getStatus" / LongNumber) { id =>
        get {
          val reply: Future[GetReply] = (myEntities ? MyEntity.Get(id)).mapTo[GetReply]
          onComplete(reply) {
            case Success(r) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"getStatus: ${r.id}: ${r.status}"))
            case Failure(e) =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Err ${e}"))
          }
        }
      }

    val bindingFuture =
      for {
        sb <- Http().bindAndHandle(route, "0.0.0.0", 8080)
      } yield {
        println("api server is running...")
        sb
      }

    sys.addShutdownHook {
      implicit val ec: ExecutionContext = system.dispatcher
      Try{ println("release port"); Await.ready(bindingFuture.flatMap(_.unbind()), Duration.Inf) }
      Try{ println("system terminate"); system.terminate() }
    }

  }

}
