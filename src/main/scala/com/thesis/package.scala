package com

import net.liftweb.json.{Formats, NoTypeHints, Serialization}

package object thesis {
  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  val ADD_TRX_ACTOR_NAME = "addTrxActor"
  val GET_TRX_ACTOR_NAME = "getTrxActor"

  val PARENT_UP = "../"
}
