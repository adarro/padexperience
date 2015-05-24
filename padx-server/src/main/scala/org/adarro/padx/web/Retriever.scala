package org.adarro.padx.web

import org.adarro.padx.padobjects.CardMeta
import net.ruippeixotog.scalascraper.browser.Browser
import com.typesafe.scalalogging.slf4j.LazyLogging

object Retriever extends LazyLogging {

  def FindCardFromDB(id: String, card: CardMeta) = {
    import org.adarro.padx.db._
    var existing = data.getExistingDocument(id)
    logger.warn("Failed to retrieve Card {} from database, attempting web", id)
    if (existing != null) Some(existing)
    else {
      FindFromWeb(id)
    }
  }

  def FindFromWeb(id: String): Option[CardMeta] = {
    val browser = new Browser
    val context = new WebContext()
    val doc = browser.get(context.Url(id))
    val card = new CardMeta(id.toInt)
    Beastiary.ReadTables(doc, card)
    import com.wix.accord._
    validate(card) match {
      case Success => Some(card)
      case _       => None
    }
  }
}