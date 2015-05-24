package org.adarro.padx.server

import scala.collection.JavaConversions.mapAsJavaMap
import org.adarro.padx.db.data
import org.adarro.padx.padobjects.CardMeta
import org.adarro.padx.padobjects.cardMetaValidator
import org.adarro.padx.web.Beastiary
import org.adarro.padx.web.Retriever
import org.adarro.padx.web.WebContext
import org.jsoup.nodes.Element
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.wix.accord.validate
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.contentExtractorAsExtractor
import net.ruippeixotog.scalascraper.dsl.DSL.deepFunctorOps
import net.ruippeixotog.scalascraper.dsl.DSL.elementAsElements
import org.scalatest.junit.JUnitRunner
import org.adarro.padx.padobjects.CardType
import org.adarro.padx.padobjects.CardElement
//import scala.collection.immutable.Map

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

  test("couchdb connection") {
    logger.debug("testing couchdb connection (connect / insert")
    import org.adarro.padx.db._
    val id = MultiTypeSingleElementCard
    var existing = data.getExistingDocument(id)
    if (existing == null) {
      logger.warn("Record already exists, insert test is invalid")

    }

    //  existing = data.getExistingDocument(id)
    if (existing == null) {
      logger.debug("no record found with id {} in local database, creating", id)
      val card = Retriever.FindFromWeb(id)
      card match {
        case Some(x) => {
          val m = Map(
            "id" -> x.id,
            "name" -> x.name,
            "cardType" -> x.cardType,
            "subType" -> x.subType,
            "element" -> x.element,
            "subElement" -> x.subElement,
            "rarity" -> x.rarity,
            "cost" -> x.cost,
            "minExperience" -> x.minExperience,
            "maxExperience" -> x.maxExperience,
            "minLevel" -> x.minLevel,
            "maxLevel" -> x.maxLevel)
          val doc = data.getDocument(id)
          import collection.JavaConversions._
          val jM = mapAsJavaMap(m).asInstanceOf[java.util.Map[java.lang.String, java.lang.Object]]
          val result = doc.putProperties(jM)
          //  logger.debug("record: {}",doc.toString())
          logger.debug("record successfully stored with rev ID {}", result.getId)

        }
        case None => logger.warn("No card using ID {} could be found, test invalid!")
      }
    } else {
      logger.warn("record with ID {} already exists, rerun test", existing.getId)
      existing.purge()
    }
  }

  test("couchdb lite retrieve to object") {
    import org.adarro.padx.db._
    val id = MultiTypeSingleElementCard
    var existing = data.getExistingDocument(id)
    if (existing == null) logger.warn("Record does not exist in DB, please be sure data is present before running this test")
    else {
      val doc = existing
      val props = doc.getProperties

      import CardType._
      import CardElement._

      //      val map = mapAsScalaMap(props).asInstanceOf[Map[String, Any]]
      logger.debug("Properties (Native) {}", props)
      logger.debug("name {} \t id {}", props.get("name"), props.get("id"))
      val name = props.get("name").asInstanceOf[String]
      
      val id = props.get("id").asInstanceOf[Int]
   //   val id2 = id.asInstanceOf[Int]
      logger.debug("id class {}", id.getClass)
   //   logger.debug("name {} \t id {}", name, id)
      val card = new CardMeta(props.get("name").asInstanceOf[String], id)
      //      card.cardType = CardType.withName(props.get("cardType").asInstanceOf[String]).asInstanceOf[CardType]
      //      card.element = CardElement.withName(props.get("element").asInstanceOf[String]).asInstanceOf[CardElement]
      //      card.cost = props.get("cost").asInstanceOf[String].toInt
      //      card.maxExperience = props.get("maxExperience").asInstanceOf[String].toInt
      //      card.minExperience = props.get("minExperience").asInstanceOf[String].toInt
      //      card.maxLevel = props.get("maxLevel").asInstanceOf[String].toInt
      //      card.minLevel = props.get("minLevel").asInstanceOf[String].toInt
      //      card.rarity = props.get("rarity").asInstanceOf[String].toInt
      //   logger.info("subelement {}, subcardtype {}", card.subElement, card.subType)
      //    logger.debug("Properties as scala map {}", map)
      logger.debug("Existing record {}\n{}", doc.getId, doc.toString())
      doc.purge()
    }
  }
}