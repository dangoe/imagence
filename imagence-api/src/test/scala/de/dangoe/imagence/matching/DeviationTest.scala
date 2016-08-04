/**
  * Copyright (c) 2016 Daniel Götten
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to
  * do so, subject to the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
package de.dangoe.imagence.matching

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
class DeviationTest extends WordSpec with Matchers {

  "A deviation" can {
    "be zero." in {
      val deviation = Deviation(0.5)

      deviation.value shouldBe 0.5
    }

    "be positive." in {
      val deviation = Deviation(0.5)

      deviation.value shouldBe 0.5
    }
  }

  it must {
    "not be negative" in {
      intercept[IllegalArgumentException] {
        Deviation(-0.1)
      }
    }
  }

  it should {

    import Implicits._

    "be equal to another deviation if its value is equal." in {
      val deviation = Deviation(0.5)
      val other = Deviation(0.5)

      Seq(deviation, other).sorted shouldBe Seq(deviation, other)
    }

    "be larger than another deviation if its value is larger." in {
      val deviation = Deviation(0.5)
      val other = Deviation(0.25)

      Seq(deviation, other).sorted shouldBe Seq(other, deviation)
    }

    "be smaller than another deviation if its value is smaller." in {
      val deviation = Deviation(0.25)
      val other = Deviation(0.5)

      Seq(other, deviation).sorted shouldBe Seq(deviation, other)
    }
  }
}
