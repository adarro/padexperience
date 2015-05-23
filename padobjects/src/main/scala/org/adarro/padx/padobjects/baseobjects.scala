package org.adarro.padx.padobjects

import scala.beans.BeanProperty

/**
 * @author creepyguy
 */

object CardElement extends Enumeration {
  type CardElement = Value
  val Fire, Water, Light, Dark, Heal = Value
}

object CardType extends Enumeration {
  type CardType = Value
  val God, Devil, Dragon, Physical, Attacker, Balanced, Healer = Value
}

trait LevelSpec {
  var minLevel: Int
  var maxLevel: Int
}

trait IdSpec {
  var name: String
  val id: Int
}

trait ExperienceSpec {
  var maxExperience: Int
  var minExperience: Int
}

trait InstanceInfo {}

trait StaticInfo {}

@BeanProperty
trait ElementInfo {
  import CardElement._
  var element: CardElement
  var subElement: CardElement
}

@BeanProperty
trait TypeSpec {
  import CardType._
  var cardType: CardType
  var subType: CardType
}

@BeanProperty
trait InstanceExperienceInfo {
  var currentExperience: Int
}

abstract class Card

import CardElement._
import CardType._
case class CardMeta(@BeanProperty var name: String, @BeanProperty id: Int) extends Card with IdSpec with LevelSpec with ElementInfo with ExperienceSpec with StaticInfo {
  @BeanProperty var element: CardElement = null
  @BeanProperty var subElement: CardElement = null
  @BeanProperty var cardType: CardType = null
  @BeanProperty var subType: CardType = null
  @BeanProperty var rarity: Int = 1
  @BeanProperty var cost: Int = 1
  @BeanProperty var minLevel: Int = 1
  @BeanProperty var maxLevel: Int = 1
  @BeanProperty var maxExperience: Int = 0
  @BeanProperty var minExperience: Int = 0
  def this(id: Int) {
    this("Unknown", id)
  }

  def this() {
    this(0)
  }

}
case class CardInstance(@BeanProperty id: Int) extends Card {
  @BeanProperty
  var level: Int = 0
  @BeanProperty
  var experience: Int = 0
}


