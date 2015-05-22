package org.adarro.padx.padobjects

import scala.beans.BeanProperty


/**
 * @author creepyguy
 */

trait LevelSpec {
  var minLevel: Int
  var maxLevel: Int
}

trait IdSpec {
  var name: String
  val id: Int
}

trait ExperienceSpec {
  val maxExperience: Int
  val minExperience: Int
}

object CardElement extends Enumeration {
  type CardElement = Value
  val Fire, Water, Light, Dark, Heal = Value
}

object CardType extends Enumeration {
  type CardType = Value
  val God, Devil, Dragon, Physical, Attacker, Balanced, Healer = Value
}

abstract class Card

import CardElement._
import CardType._
case class CardMeta(@BeanProperty var name: String, @BeanProperty var element: CardElement = null, @BeanProperty var subElement: CardElement = null, @BeanProperty var cardType: CardType = null, @BeanProperty var subType: CardType = null, @BeanProperty var rarity: Int = 1, @BeanProperty var cost: Int = 1, @BeanProperty id: Int, @BeanProperty  var minLevel: Int, @BeanProperty var maxLevel: Int = 1) extends Card with IdSpec with LevelSpec {
  def this() {
    this(name = "Unknown", null, null, null, null, 0, 0, 0, 0, 0)
  }
  
  def this(id :Int) {
    this(null, null, null, null, null, 0, 0, 0, 0, id)
  }
}
case class CardInstance(id: Int, var level: Int, var experience: Int = 0) extends Card
