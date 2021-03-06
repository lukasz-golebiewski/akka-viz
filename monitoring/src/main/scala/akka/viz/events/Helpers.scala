package akka.viz.events

import akka.actor.ActorRef

trait Helpers {
  implicit def actorRefToString(ar: ActorRef): String = {
    val path = ar.path.toSerializationFormat
    val idPos = path.indexOf('#')
    if (idPos >= 0) path.substring(0, idPos) else path
  }

  implicit class IsUserActorRef(underlying: ActorRef) {
    def isUserActor: Boolean = {
      val elems = underlying.path.elements
      elems.size > 1 && elems.exists(_ == "user")
    }
  }

}

object Helpers extends Helpers
