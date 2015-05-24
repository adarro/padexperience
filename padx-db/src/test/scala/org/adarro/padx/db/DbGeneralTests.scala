package org.adarro.padx.db

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.typesafe.config.ConfigFactory
import org.adarro.padx.padobjects.CardMeta
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.junit.{ Rule, ClassRule }
import org.junit.rules._
import org.json4s.JsonFormat
@RunWith(classOf[JUnitRunner])
class DbGeneralTest extends FunSuite with org.scalatest.Matchers with LazyLogging {

  val MultiTypeMultiElementCard = "761"
  val MultiTypeSingleElementCard = "302"
  val SingleTypeSingleElementCard = "300"
  //  @Rule
  //  implicit val watcher: TestRule = new NamedWatcher
  case class Child(name: String, age: Int, birthdate: Option[java.util.Date])
  case class Winnering(id: String, stuff: String, numbers: Option[Int])
  case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winnering], drawDate: Option[java.util.Date])
  case class simpleCaseClass(id: Int, name: String)
  object CardElement extends Enumeration {
    type CardElement = Value
    val Fire, Water, Light, Dark, Heal = Value
  }

  object CardType extends Enumeration {
    type CardType = Value
    val God, Devil, Dragon, Physical, Attacker, Balanced, Healer = Value
  }

  case class CardMeta(var name: String, id: Int) {
    import CardElement._
    import CardType._
    var element: CardElement = null
    var subElement: CardElement = null
    var cardType: CardType = null
    var subType: CardType = null
    var rarity: Int = 1
    var cost: Int = 1
    var minLevel: Int = 1
    var maxLevel: Int = 1
    var maxExperience: Int = 0
    var minExperience: Int = 0
    def this(id: Int) {
      this("Unknown", id)
    }

    def this() {
      this(0)
    }

  }

  test("JSoN Serialization check with JSoN4S") {
    import org.json4s._
    import org.json4s.native.JsonMethods._
    import org.json4s.JsonDSL._
    implicit val formats = org.json4s.DefaultFormats
    val json = ("name" -> "joe") ~ ("age" -> 35)

    case class MyBean(name: String, age: Int)
    //val myBean = MyBean("joe", 35)

    val doc = render(json)
    logger.debug("doc: {}", doc)
    val r = pretty(doc)
    logger.debug("bean result: {}", r)
    r should not be empty
  }

  test("JSoN Serialization check with lift-json") {
    import net.liftweb.json.JsonAST._
    import net.liftweb.json.Extraction._
    import net.liftweb.json.Printer._
    import net.liftweb.json.JsonDSL._
    implicit val formats = net.liftweb.json.DefaultFormats
    val json = ("name" -> "joe") ~ ("age" -> 35)

    case class MyBean(name: String, age: Int)
    //val myBean = MyBean("joe", 35)

    val doc = render(json)
    logger.debug("doc: {}", doc)
    val r = pretty(doc)
    logger.debug("bean result: {}", r)
    r should not be empty
  }
  test("json4s complex") {
    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.native.JsonMethods._
    implicit val formats = net.liftweb.json.DefaultFormats
    //import org.json4s.jackson.JsonMethods._

    case class Winner(id: Long, numbers: List[Int])
    case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[java.util.Date])

    val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
    val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)

    val json =
      ("lotto" ->
        ("lotto-id" -> lotto.id) ~
        ("winning-numbers" -> lotto.winningNumbers) ~
        ("draw-date" -> lotto.drawDate.map(_.toString)) ~
        ("winners" ->
          lotto.winners.map { w =>
            (("winner-id" -> w.id) ~
              ("numbers" -> w.numbers))
          }))

    val result = compact(render(json))
    logger.debug("Complex result {}", result)
  }
  test("json4s complex direct cast") {
    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.native.JsonMethods._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{ read, write }
    implicit val formats = Serialization.formats(NoTypeHints)

    //import org.json4s.jackson.JsonMethods._

    //  val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
    //  val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)
    // val winner = Winner("23", List(2, 45, 34, 23, 3, 5)) //winners(0)
    val winner = Winnering("23", "Jordan", None) //winners(0)
    // type DslConversion = Winner => JValue

    //  val json = winners(0)
    //           ("winners" ->
    //              lotto.winners.map { w =>
    //                (("winner-id" -> w.id) ~
    //                  ("numbers" -> w.numbers))
    //              })
    // val json = lotto.asInstanceOf[JsonAST.JObject]
    val child = Child("Mary", 5, None)
    val card = CardMeta("302", 12)
    import CardElement._
    card.element = CardElement.Light

    //    val ser = write(Child("Mary", 5, None))
    //    read[Child](ser)
    val result = Extraction.decompose(card)
    //    val result = compact(render(Extraction.decompose(json)))
    logger.debug("decomposed result {}", result)
    val result2 = compact(render(result))
    logger.debug("rendered result {}\n", result2)
  }

  class SimpleBean(val id: String, val data: String)
  class SimpleBeanWithProperties(val id: String, val data: String) {
    var extraData: Option[String] = None
  }

  import spray.json.RootJsonFormat
  object MyJsonProtocol extends spray.json.DefaultJsonProtocol {
    implicit object ColorJsonFormat extends RootJsonFormat[SimpleBean] {
      import spray.json._
      def write(c: SimpleBean) =
        JsArray(JsString(c.id), JsString(c.data))

      def read(value: JsValue) = value match {
        case JsArray(Vector(JsString(id), JsString(data))) =>
          new SimpleBean(id, data)
        case _ => deserializationError("SimpleBean expected")
      }
    }
    implicit object SimpleBeanWithPropertiesFormat extends RootJsonFormat[SimpleBeanWithProperties] {
      import spray.json._
      def write(c: SimpleBeanWithProperties) = {
        JsArray(JsString(c.id), JsString(c.data), c.extraData match {
          case Some(x: String) => JsString(x)
          case _               => JsString("")
        })
      }

      def read(value: JsValue) = value match {
        case JsArray(Vector(JsString(id), JsString(data))) => {
          val r = new SimpleBeanWithProperties(id, data)
          r
        }
        case JsArray(Vector(JsString(id), JsString(data), JsString(extra))) => {

          val opt = if (extra == "") None else Some(extra)
          val r = new SimpleBeanWithProperties(id, data)
          r.extraData = opt
          r
        }
        case _ => deserializationError("SimpleBeanWithProperties expected")
      }
    }
  }

  test("spray-json - normal bean class") {
    import spray.json._
    import spray.json.DefaultJsonProtocol
    import MyJsonProtocol._
    // implicit val cardFormat = jsonFormat2(CardMeta)

    val child = Child("Mary", 5, None)
    val card = CardMeta("302", 12)
    card.element = CardElement.Light
    val simple = new SimpleBean("23", "Testing")
    //val json =  card.toJson
    // import CardElement._
    logger.debug("SimpleBean {}", simple)
    val json = simple.toJson
    logger.debug("json result {}", json)

    //    read[Child](ser)
    val result2 = json.convertTo[SimpleBean]
    logger.debug("back as Object {}", result2)
    //    val result = compact(render(Extraction.decompose(json)))

    val result = json.prettyPrint
    logger.debug("rendered result {}\n", result)
  }

  test("spray-json case class") {
    import spray.json._
    //  import spray.json.DefaultJsonProtocol
    import spray.json.DefaultJsonProtocol._
    implicit val formatter = jsonFormat2(simpleCaseClass)
    val cclass = new simpleCaseClass(302, "Drawn Jester")
    val json = cclass.toJson
    logger.debug("case class to json: {}", json)
    val bean = json.convertTo[simpleCaseClass]
    logger.debug("json to case class: {}", bean)
  }
  test("spray-json - normal bean class with properties") {
    import spray.json._
    import spray.json.DefaultJsonProtocol
    import MyJsonProtocol._
    // implicit val cardFormat = jsonFormat2(CardMeta)
    val simple = new SimpleBeanWithProperties("23", "Testing")
    simple.extraData = Some("Fancy")
    //val json =  card.toJson
    // import CardElement._
    logger.debug("SimpleBean {}", simple)
    val json = simple.toJson
    logger.debug("json result {}", json)

    //    read[Child](ser)
    val result2 = json.convertTo[SimpleBeanWithProperties]
    logger.debug("back as Object extra data {}", result2.extraData)
    //    val result = compact(render(Extraction.decompose(json)))

    val result = json.prettyPrint
    logger.debug("rendered result {}\n", result)
  }
  test("Spray-json case class with Option[x]") {
    import spray.json._
    import spray.json.DefaultJsonProtocol._

    // string instead of date for simplicity
    case class SearchRequest(url: String, nextAt: Option[String])

    // btw, you could use jsonFormat2 method here
    // implicit val searchRequestFormat = jsonFormat(SearchRequest, "url", "nextAt")
    implicit val searchRequestFormat = jsonFormat2(SearchRequest)
    val list = List(
      """{"url":"..."}""",
      """{"url":"...", "nextAt":null}""",
      """{"url":"...", "nextAt":"2012-05-30T15:23Z"}""")
      .map(_.parseJson.convertTo[SearchRequest])
    val result = List(
      SearchRequest("...", None),
      SearchRequest("...", None),
      SearchRequest("...", Some("2012-05-30T15:23Z")))
    logger.debug("\nList: {}\nResult: {}", list, result)
    assert {
      list == result
    }
  }
  
  

  

  class NamedWatcher extends TestWatcher() {
    import org.junit.runner.Description
    protected override def starting(description: Description): Unit = {
      println("Starting test: " + description.getMethodName());
    }
  }

}