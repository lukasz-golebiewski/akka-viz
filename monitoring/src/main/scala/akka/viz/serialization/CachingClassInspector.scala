package akka.viz.serialization

import scala.collection.mutable

object CachingClassInspector {

  private val cache = mutable.Map[Class[_], ClassInspector]()

  def of(clazz: Class[_]): ClassInspector = {
    cache.getOrElseUpdate(clazz, ClassInspector.of(clazz))
  }

}

