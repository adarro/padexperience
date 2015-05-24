package org.adarro.padx

 import com.wix.accord._
  import dsl._
  
package object padobjects {
  import org.adarro.padx.padobjects.CardMeta
  implicit val cardMetaValidator = validator[CardMeta] {
    x =>
      x.name is notEmpty
      x.id should be > 0
      x.element is notNull
      //non-null subelement with null element should be invalid
      x.cardType is notNull      
      x.maxExperience >= x.minExperience
      x.maxExperience >= x.minLevel
      x.rarity > -1
      x.cost should be > -1      
  }
  
  implicit val cardInstanceValidator = validator[CardInstance] {
    x =>
      x.id should be > -1
      x.experience should be > -1
      x.level should be > -1
  }

}