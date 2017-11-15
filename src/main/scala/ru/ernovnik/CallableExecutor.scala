package ru.ernovnik

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.{Callable, TimeUnit}
import akka.actor.{Actor, ActorSystem, Props}
import akka.dispatch.{Envelope, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import scala.concurrent.duration._

object CallableExecutor extends App {
  val actorSystem = ActorSystem("ActorSystem")
  val priorityActor = actorSystem.actorOf(Props[PriorityActor])
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test8"
    }
  }, LocalDateTime.now.plusSeconds(4))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test9"
    }
  }, LocalDateTime.now.plusSeconds(5))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test7"
    }
  }, LocalDateTime.now.plusSeconds(3))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test6"
    }
  }, LocalDateTime.now.plusSeconds(2))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test5"
    }
  }, LocalDateTime.now.plusSeconds(1))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test4"
    }
  }, LocalDateTime.now.plusSeconds(1))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test3"
    }
  }, LocalDateTime.now.plusSeconds(1))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test2"
    }
  }, LocalDateTime.now.plusSeconds(-1))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test1"
    }
  }, LocalDateTime.now.plusSeconds(-1))
  priorityActor ! (new Callable[String] {
    override def call() = {
      Thread.sleep(1000); "test0"
    }
  }, LocalDateTime.now.plusSeconds(-2))
  Thread.sleep(20000)
  actorSystem.terminate
}

class MyPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox({
  case (Envelope(msg1, _), Envelope(msg2, _)) =>
    (msg1, msg2) match {
      case ((c1: Callable[_], l1: LocalDateTime, id1: Int), (c2: Callable[_], l2: LocalDateTime, id2: Int)) =>
        val v = l1.compareTo(l2)
        if (v == 0) id1.compareTo(id2) else v
      case _ => 0
    }
})

class PriorityActor extends Actor {
  val schedulerActor = context.actorOf(Props[SchedulerActor].withDispatcher("prio-dispatcher"))
  var id = 0

  def receive = {
    case (c: Callable[_], l: LocalDateTime) ⇒ schedulerActor ! (c, l, id); id = id + 1
    case x => println(x)
  }
}

class SchedulerActor extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  val executorActor = context.actorOf(Props[ExecutorActor])

  def receive = {
    case (c: Callable[_], l: LocalDateTime, id: Int) ⇒ {
      val timeToStart = math.max(l.toInstant(ZoneOffset.UTC).toEpochMilli - LocalDateTime.now.toInstant(ZoneOffset.UTC).toEpochMilli, 0)
      val delay = new FiniteDuration(timeToStart, TimeUnit.MILLISECONDS)
      context.system.scheduler.scheduleOnce(delay) {
        executorActor ! c
      }
    }
  }
}

class ExecutorActor extends Actor {
  def receive = {
    case c: Callable[_] => println(c.call())
  }
}