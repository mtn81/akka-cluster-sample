package myapp.sharding

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.pattern.pipe
import myapp.sharding.SubTask.UpdateState

import scala.concurrent.Future
import scala.concurrent.duration._

abstract class SubTask[S] extends Actor {

  implicit val ec = context.system.dispatcher

  override def receive =
    waitForUpdate()

  def waitForUpdate(
    state: Option[S] = None
  ): Receive = {

    case MyEntity.UpdateTask(id) =>
      context.become(
        updateScheduling(
          schedule = context.system.scheduler.scheduleOnce(10 seconds, self, SubTask.Update(id)),
          state = state
        ),
      )

    case SubTask.GetState(id, receiver) =>
      if(state.nonEmpty)
        receiver ! state.get
      else {
        val s: Future[S] = updateState(id, state)
        s pipeTo receiver
        s.map(s => UpdateState(id, s)) pipeTo self
      }

    case SubTask.UpdateState(id, s: S) =>
      context.become(
        waitForUpdate(
          state = Some(s)
        )
      )
  }

  def updateScheduling(
    schedule: Cancellable,
    state: Option[S] = None
  ): Receive = {

    case MyEntity.UpdateTask(id) =>
      schedule.cancel()
      context.become(
        updateScheduling(
          schedule = context.system.scheduler.scheduleOnce(10 seconds, self, SubTask.Update(id))
        )
      )

    case SubTask.Update(id) =>
      context.become(
        updateProcessing(
          state = state
        )
      )
      updateState(id, state).map(s => UpdateState(id, s)) pipeTo self

    case SubTask.GetState(id, receiver) =>
      schedule.cancel()
      context.become(
        updateProcessing(state = state)
      )
      val s: Future[S] = updateState(id, state)
      s pipeTo receiver
      s.map(s => UpdateState(id, s)) pipeTo self

    case SubTask.UpdateState(id, s: S) =>
      context.become(
        updateScheduling(
          schedule = schedule,
          state = Some(s)
        )
      )
  }

  def updateProcessing(
    state: Option[S] = None
  ): Receive = {

    case MyEntity.UpdateTask(id) =>
      context.become(
        updateScheduling(
          schedule = context.system.scheduler.scheduleOnce(10 seconds, self, SubTask.Update(id)),
          state = state
        )
      )

    case SubTask.GetState(id, receiver) =>
      val s: Future[S] = updateState(id, state)
      s pipeTo receiver
      s.map(s => UpdateState(id, s)) pipeTo self

    case SubTask.UpdateState(id, s: S) =>
      context.become(
        waitForUpdate(
          state = Some(s)
        )
      )

  }

  protected def updateState(id: Long, old: Option[S]): Future[S]
}

object SubTask {
  case class Update(id: Long)
  case class UpdateState[S](id: Long, state: S)

  case class GetState[S](id: Long, receiver: ActorRef)
  case class GetStateReply[S](id: Long, state: S)


}

class SubTask1 extends SubTask[Int] {
  import akka.pattern.ask

  override def receive = super.receive.orElse {
    case MyEntity.Get1(id) => ???
  }

  override protected def updateState(id: Long, old: Option[Int]): Future[Int] =
    old.map { s => Future { s + 1 } }.getOrElse(Future { 1 })
}


class SubTask2 extends SubTask[Long] {

  override def receive = super.receive.orElse {
    case MyEntity.Get2(id) => ???
  }

  override protected def updateState(id: Long, old: Option[Long]): Future[Long] =
    old.map { s => Future { s + 1 } }.getOrElse(Future { 1 })
}
