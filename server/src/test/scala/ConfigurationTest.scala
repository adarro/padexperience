package org.adarro.padx.server

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.typesafe.config.ConfigFactory
import org.adarro.padx.padobjects.CardMeta
import com.typesafe.scalalogging.slf4j.LazyLogging

@RunWith(classOf[JUnitRunner])
class ConfigurationSuite extends FunSuite with org.scalatest.Matchers with LazyLogging {

  //var id = "302" // Drawn Jester, a multi type , single element monster
  //  var id = "761" // Jester Dragon, Drawn Jester, a multi type , multi element monster
  val MultiTypeMultiElementCard = "761"
  val MultiTypeSingleElementCard = "302"
  val SingleTypeSingleElementCard = "300"
  test("Beastiary URL should be present") {
    val context = ConfigFactory.load()
    context.getString("padweb-lib.beastiary") should be("http://puzzledragonx.com/en/monster.asp?n=")

  }

  test("Min Level should be derived from id") {
    import net.ruippeixotog.scalascraper.browser.Browser
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
    val browser = new Browser
    val context = new WebContext()
    val id = SingleTypeSingleElementCard
    val doc = browser.get(context.Url(id))
    //for some reason, searching directly on class .minlevel fails... so we go to the one before
    val r = doc >?> element(".statlevel")
    r match {
      case Some(ele) => {
        logger.debug("Min: {}", ele.nextElementSibling().html)
        logger.debug("Max: {}", ele.nextElementSibling().nextElementSibling().html)
      }
      case _ =>
    }

  }
  test("Beastiary url should be derived from id") {
    val context = new WebContext()
    val id = MultiTypeMultiElementCard
    context.Url(id) should be("http://puzzledragonx.com/en/monster.asp?n=" + id)
  }

  test("Beastiary url should be retrievable from id") {
    import net.ruippeixotog.scalascraper.browser.Browser

    val browser = new Browser
    val context = new WebContext()
    val id = SingleTypeSingleElementCard
    val doc = browser.get(context.Url(id))
    import org.jsoup.nodes.Element
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
    val div: Option[Element] = doc >?> element(".avatar")
    var name: String = ""
    div match {
      case Some(ele) => {
        val imgTag = ele.getElementsByTag("img").first()
        name = imgTag.attr("title")
      }
      case _ => None
    }
    logger.info("Div HTML:\n {}", div)

    div should be('defined)

  }
  test("Beastiary types, elements, rarity and cost should be retrievable from id") {
    import net.ruippeixotog.scalascraper.browser.Browser

    val browser = new Browser
    val context = new WebContext()
    val id = MultiTypeMultiElementCard
    val doc = browser.get(context.Url(id))
    val card = new CardMeta(id.toInt)
    logger.debug("attempting to read cardtype before defining {}", card.cardType)
    Beastiary.ReadTables(doc, card)

    card.cardType should not be null
    card.subType should not be null
    card.element should not be null
    card.subElement should not be null
    card.rarity should be > 0
    card.cost should be > 0
    card.minLevel should be > 0
    card.maxLevel should be > 0
    import org.adarro.padx.padobjects._
    import com.wix.accord._
    val result = validate(card)

  }

  test("Name should be retrievable from id") {
    import net.ruippeixotog.scalascraper.browser.Browser
    val id = SingleTypeSingleElementCard
    val browser = new Browser
    val context = new WebContext()
    val doc = browser.get(context.Url(id))
    val card = new CardMeta

    Beastiary.ReadTables(doc, card)
    card.name should not be "Unknown"
  }

  test("Beastiary Single Element Types should not have subtype") {
    import net.ruippeixotog.scalascraper.browser.Browser
    val id = SingleTypeSingleElementCard
    val browser = new Browser
    val context = new WebContext()

    val doc = browser.get(context.Url(id))
    val card = new CardMeta

    Beastiary.ReadTables(doc, card)
    card.cardType should not be null
    card.subType should be(null)
    card.element should not be null
    card.subElement should be(null)
    card.rarity should be > 0
    card.cost should be > 0
  }

  test("Beastiary Single Card Types should not have subtype") {
    import net.ruippeixotog.scalascraper.browser.Browser
    val id = SingleTypeSingleElementCard
    val browser = new Browser
    val context = new WebContext()

    val doc = browser.get(context.Url(id))
    val card = new CardMeta
    logger.debug("attempting to read cardtype before defining {}", card.cardType)
    Beastiary.ReadTables(doc, card)
    card.cardType should not be null
    card.subType should be(null)
    card.element should not be null
    card.subElement should be(null)
    card.rarity should be > 0
    card.cost should be > 0
  }
}