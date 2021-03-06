package akka.viz.events

import akka.actor.ActorRef
import akka.viz.events.Helpers._
import akka.viz.events.types._

import scala.Predef.{any2stringadd => _, _}
import scala.language.implicitConversions

case class LightSnapshot(
    liveActors: Set[String] = Set(),
    children: Map[String, Set[String]] = Map(),
    receivedFrom: Set[(String, String)] = Set()
) {

  implicit def refPair2StringPair(pair: (ActorRef, ActorRef)): (String, String) = {
    val (actor1, actor2) = pair
    (actorRefToString(actor1), actorRefToString(actor2))
  }

  def dead: Set[String] = {
    liveActors diff (children.values.flatten ++ receivedFrom.flatMap(p => Seq(p._1, p._2))).toSet
  }

  def update(ev: BackendEvent): LightSnapshot = ev match {
    case ReceivedWithId(_, from, to, _) =>
      val live: Set[String] = liveActors ++ Set[ActorRef](from, to).filter(_.isUserActor).map(actorRefToString)
      val recv = receivedFrom + (from -> to)
      copy(liveActors = live, receivedFrom = recv)
    case Spawned(ref, parent) =>
      if (ref.isUserActor) {
        val live = liveActors + ref
        val childr = children.updated(parent, children.getOrElse(parent, Set()) + ref)
        copy(liveActors = live, children = childr)
      } else {
        this
      }
    case Killed(ref) if ref.isUserActor =>
      copy(liveActors = liveActors - ref)
    case CurrentActorState(ref, _) if ref.isUserActor =>
      copy(liveActors = liveActors + ref)
    case Instantiated(ref, _) if ref.isUserActor =>
      copy(liveActors = liveActors + ref)
    case other =>
      this
  }
}
