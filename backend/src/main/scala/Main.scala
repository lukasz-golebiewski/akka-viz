import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props, ActorSystem}
import akka.util.Timeout
import fsm.DiningHakkersOnFsm
import postoffice.PostOffice

import scala.util.Random

object Main extends App {
  DiningHakkersOnFsm.run(ActorSystem("fsm"))
  PostOffice.run(ActorSystem("post-office"))

  val system = ActorSystem("small-demos")
  val lazyActorProps = Props(new Actor {
    override def receive: Receive = {
      case msg =>
        Thread.sleep(Random.nextInt(2000))
        sender() ! msg
    }
  })

  val lazyActor1 = system.actorOf(lazyActorProps, "lazy1")
  val lazyActor2 = system.actorOf(lazyActorProps, "lazy2")
  for (i <- 0 to 1000) {
    lazyActor1.tell("doit", lazyActor2)
  }

  def numberActorProps = Props(new Actor {
    override def receive: Actor.Receive = {
      case "give me a number" =>
        sender() ! Random.nextInt(10)
    }
  })

  val numbersActor = system.actorOf(numberActorProps, "numberActor")
  for (_ <- 1 to 10) {
    import akka.pattern._
    implicit val timeout = Timeout(1, TimeUnit.SECONDS)
    import scala.concurrent.ExecutionContext.Implicits.global
    
    (numbersActor ? "give me a number").onSuccess { case n: Any => println(s"got $n") }
  }
}
