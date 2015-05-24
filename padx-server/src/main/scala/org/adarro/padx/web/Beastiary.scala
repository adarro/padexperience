package org.adarro.padx.web

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

import org.adarro.padx.padobjects.Card
import org.adarro.padx.padobjects.CardElement
import org.adarro.padx.padobjects.CardElement.CardElement
import org.adarro.padx.padobjects.CardMeta
import org.adarro.padx.padobjects.CardType
import org.adarro.padx.padobjects.CardType.CardType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import com.typesafe.config._
import com.typesafe.scalalogging.slf4j.LazyLogging

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.asScalaBuffer
import net.ruippeixotog.scalascraper.dsl.DSL.contentExtractorAsExtractor
import net.ruippeixotog.scalascraper.dsl.DSL.deepFunctorOps
import net.ruippeixotog.scalascraper.dsl.DSL.elementAsElements

object Beastiary extends App with LazyLogging {
  private val context = new WebContext()

  import net.ruippeixotog.scalascraper.browser.Browser

  import org.adarro.padx.padobjects.Card
  import org.adarro.padx.padobjects.CardMeta
  import org.adarro.padx.padobjects.CardInstance

  import org.jsoup.nodes.Element
  import org.jsoup.nodes.Document
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

  def Page(id: String): Option[Card] = {
    val browser = new Browser
    val doc = browser.get(context.Url(id))
    val divImg: Option[Element] = doc >?> element(".avatar")
    var imgUrl: String = ""
    val name = divImg match {
      case Some(ele) => {
        val imgTag = ele.getElementsByTag("img").first()
        imgUrl = imgTag.attr("src")
        imgTag.attr("title")
      }
      case _ => ""
    }

    None
  }

  
  
  def ReadTables(doc: Document, c: CardMeta) = {

    import org.adarro.padx.padobjects.CardType._
    val tables = doc.getElementById("compareprofile")
    val t = tables.getElementsByTag("table")
    val nameProfile = t(1)
    val nameRows = nameProfile.select("tr")
    for (row <- nameRows) {
      val heading = row.child(0).text()
      logger.debug("header is {}", heading)
      logger.debug("evaluating {}", row)
      heading match {
        case x: String if (x == "Name:") => {
          val data = row.select("td.data")
          logger.debug("data is {}", data)
          c.name = data(0).html
          logger.debug("Name set to {}", c.name)
        }
        case x: String if (x == "JP Name:") => {
          //TODO: Add property for JP Name
          //            val data = row.select("td.data")
          //          logger.debug("data is {}", data)
          //          c.name = data(0).html
          //          logger.debug("Name set to {}", c.name)
        }
        case _ =>
      }
    }
    //type, element, rarity and cost
    val profile = t(2)
    logger.info("Profile \n{}", profile)
    val rows = profile.select("tr")

    import org.adarro.padx.padobjects.CardElement._
    import org.adarro.padx.padobjects.CardElement
    import org.adarro.padx.padobjects.CardType
    import org.adarro.padx.padobjects.CardType._

    for (row <- rows) {
      val heading = row.child(0).text()
      logger.debug("header is {}", heading)
      logger.debug("evaluating {}", row)
      heading match {
        case "Type:" => {
          val data = row.select("td.data").select("a")
          if (data.size() > 0) {
            logger.debug("found {} types of type data", data.size().toString())
            val v = data(0).text
            logger.debug("Potential type found: {}", v)
            try {
              c.cardType = CardType.withName(v).asInstanceOf[CardType]
              logger.debug("CardType set to {}", c.cardType)
            } catch { case ex: NoSuchElementException => logger.error("Expected valid card type not found", ex) }
            if (data.size() > 1) {
              val v = data(1).text
              logger.debug("Potential type found: {}", v)
              try {
                c.subType = CardType.withName(v).asInstanceOf[CardType]
                logger.debug("Card SubType set to {}", c.subType)
              } catch { case ex: NoSuchElementException => logger.error("Expected valid card type not found", ex) }
            }
          }
        }
        case "Element:" => {
          val data = row.select("td.data").select("a")
          if (data.size() > 0) {
            logger.debug("found {} types of element data", data.size().toString())
            val v = data(0).text
            logger.debug("Potential element found: {}", v)
            try {
              c.element = CardElement.withName(v).asInstanceOf[CardElement]
              logger.debug("Element set to {}", c.element)
            } catch { case ex: NoSuchElementException => logger.error("Expected valid element type not found", ex) }
            if (data.size() > 1) {
              val v = data(1).text
              logger.debug("Potential type found: {}", v)
              try {
                c.subElement = CardElement.withName(v).asInstanceOf[CardElement]
                logger.debug("Element SubType set to {}", c.subElement)
              } catch { case ex: NoSuchElementException => logger.error("Expected valid element type not found", ex) }
            }
          }
        }

        case "Rarity:" => {
          val data = row.select("td.data").select("a")
          if (data.size() > 0) {
            logger.debug("found {} rarity data", data.size().toString())
            val v = data(0).text
            logger.debug("Potential rarity found: {}", v)
            // Pattern is 'n Stars', so we just want the number
            import scala.util.matching.Regex
            import scala.util.matching.Regex._

            val pattern = new Regex("""(\d+) Stars""", "rarity")

            pattern.findFirstMatchIn(v) match {
              case x: Some[Match] => {
                c.rarity = x.get.group("rarity").toInt
                logger.debug("Rarity set to {}", c.rarity.toString())
              }
              case _ => logger.warn("Rarity Match not found")
            }
          }
        }
        case "Cost:" => {
          val data = row.select("td.data").select("a")
          if (data.size() > 0) {
            logger.debug("found {} Cost data", data.size().toString())
            val v = data(0).text
            logger.debug("Potential Cost found: {}", v)
            c.cost = v.toInt
            logger.debug("Cost set to {}", c.cost.toString())
          }
        }
        case _ =>
      }
      //  logger.debug("other rows: {}", row.child(0).text())
    }
    //find min / max level
    val r = doc >?> element(".statlevel")
    r match {
      case Some(ele) => {
        val minLevel = ele.nextElementSibling().html
        val maxLevel = ele.nextElementSibling().nextElementSibling().html
        c.minLevel = minLevel.toInt
        c.maxLevel = maxLevel.toInt
        logger.debug("Min: {} / Max {}", c.minLevel.toString(), c.maxLevel.toString())
      }
      case _ =>
    }
    logger.debug("Card Results {}", c)

  }

}

// Whenever you write a library, allow people to supply a Config but
// also default to ConfigFactory.load if they don't supply one.
// Libraries generally have some kind of Context or other object
// where it's convenient to place the configuration.

// we have a constructor allowing the app to provide a custom Config
class WebContext(config: Config) {

  // This verifies that the Config is sane and has our
  // reference config. Importantly, we specify the "simple-lib"
  // path so we only validate settings that belong to this
  // library. Otherwise, we might throw mistaken errors about
  // settings we know nothing about.
  config.checkValid(ConfigFactory.defaultReference(), "padweb-lib")

  // This uses the standard default Config, if none is provided,
  // which simplifies apps willing to use the defaults
  def this() {
    this(ConfigFactory.load())
  }

  val Source = config.getString("padweb-lib.beastiary")

  def Url(id: String): String = "%s%s".format(Source, id)

  // this is the amazing functionality provided by simple-lib
  def printSetting(path: String) {
    println("The setting '" + path + "' is: " + config.getString(path))
  }
}