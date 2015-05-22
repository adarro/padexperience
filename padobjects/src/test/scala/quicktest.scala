package org.adarro.padx.padobjects

/**
 * @author creepyguy
 */

import scala.collection.mutable.Stack
import org.scalatest.Assertions
import org.junit.Test

class StackSuite extends Assertions {

  @Test def stackShouldPopValuesIinLastInFirstOutOrder() {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  @Test def stackShouldThrowNoSuchElementExceptionIfAnEmptyStackIsPopped() {
    val emptyStack = new Stack[String]
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }

  }
}

/*
Here's an example of a FunSuite with ShouldMatchers mixed in:
*/
import org.scalatest.FunSuite
//import org.scalatest.matchers.ShouldMatchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class ListSuite extends FunSuite with org.scalatest.Matchers {

  test("An empty list should be empty") {
    List() should be('empty)
    Nil should be('empty)
  }

  test("A non-empty list should not be empty") {
    List(1, 2, 3) should not be ('empty)
    List("fee", "fie", "foe", "fum") should not be ('empty)
  }

  test("A list's length should equal the number of elements it contains") {
    List() should have length (0)
    List(1, 2) should have length (2)
    List("fee", "fie", "foe", "fum") should have length (4)
  }
}

/*
ScalaTest also supports the behavior-driven development style, in which you
combine tests with text that specifies the behavior being tested. Here's
an example whose text output when run looks like:

A Map
- should only contain keys and values that were added to it
- should report its size as the number of key/value pairs it contains
*/
import org.scalatest.FunSpec
import scala.collection.mutable.Stack

class ExampleSpec extends FunSpec {

  describe("A Stack") {

    it("should pop values in last-in-first-out order") {
      val stack = new Stack[Int]
      stack.push(1)
      stack.push(2)
      assert(stack.pop() === 2)
      assert(stack.pop() === 1)
    }

    it("should throw NoSuchElementException if an empty stack is popped") {
      val emptyStack = new Stack[Int]
      intercept[NoSuchElementException] {
        emptyStack.pop()
      }
    }
  }
  import CardElement._
  import CardType._
  @Test def cardInstanceShouldBeInstanciated() {
    val cardI = CardInstance(3, 23)
    val cardM = CardMeta("Drawn Joker", Dark, null, Dragon, Devil, 5, 15, 302, 1, 99)
    def printCard(card: Card) {
      card match {
        case CardInstance(i, l, e) => {
          println("card Id is:" + i)
        }
        case CardMeta(n, e, se, t, st, r, c, id, min, max) =>
          println("Meta Data found for " + n)

      }
    }

  }
}
