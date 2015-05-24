package org.adarro.padx.db

import org.junit.ClassRule
import org.junit.rules.TestWatcher
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.junit.runner.RunWith
import org.junit.rules.ExternalResource
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * @author creepyguy
 */
object ScalaClassRuleTest extends LazyLogging {
  @ClassRule def externalResource = new TestWatcher() {
    import org.junit.runner.Description
    protected override def starting(description: Description): Unit = {
      logger.debug("Starting test: {}", description.getMethodName());
    }
  }
}

object ScalaClassRuleTest2 extends LazyLogging {
  @ClassRule def externalResource = new ExternalResource() {
    protected override def before() = logger.debug("before")
    protected override def after() = logger.debug("after")
  }
}

@RunWith(classOf[JUnitRunner])
class Db2GeneralTestSuite extends FunSuite with org.scalatest.Matchers with LazyLogging {
  import org.junit.Test
   implicit val r = ScalaClassRuleTest
  @Test def test() {
    logger.debug("ClassRule test should run")
    logger.debug("a test")
  }
  test("here be wagons") {
   
    logger.debug("and wigers and wheres oh my!")
  }
}