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
package de.dangoe.imatch

import de.dangoe.imatch.Colors._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
class ColorsTest extends WordSpec with Matchers {

  val color = Color(50, 100, 150, 200)

  "Red channel value" can {
    "be read." in {
      color.red shouldBe 50
    }
  }

  it must {
    "not be a negative number." in {
      intercept[IllegalArgumentException] {
        Color(-1, 0, 0)
      }
    }

    "not be a number greater than 255." in {
      intercept[IllegalArgumentException] {
        Color(256, 0, 0)
      }
    }
  }

  "Green channel value" can {
    "be read." in {
      color.green shouldBe 100
    }
  }

  it must {
    "not be a negative number." in {
      intercept[IllegalArgumentException] {
        Color(0, -1, 0)
      }
    }

    "not be a number greater than 255." in {
      intercept[IllegalArgumentException] {
        Color(0, 256, 0)
      }
    }
  }

  "Blue channel value" can {
    "be read." in {
      color.blue shouldBe 150
    }
  }

  it must {
    "not be a negative number." in {
      intercept[IllegalArgumentException] {
        Color(0, 0, -1)
      }
    }

    "not be a number greater than 255." in {
      intercept[IllegalArgumentException] {
        Color(0, 0, 256)
      }
    }
  }

  "Transparency check" should {
    "return true" when {
      "alpha value is smaller than 255." in {
        for (a <- 0 until 255)
          Color(0, 0, 0, a).withTransparency shouldBe true
      }
    }

    "return false" when {
      "alpha value is equal to 255." in {
        Color(0, 0, 0, 255).withTransparency shouldBe false
      }
    }
  }

  "Alpha channel value" can {
    "be read." in {
      color.alpha shouldBe 200
    }
  }

  "Red channel" can {
    "be extracted from an arbitrary RGB value." in {
      Red.extract(color.toRGB) shouldBe 50
    }
  }

  "Green channel" can {
    "be extracted from an arbitrary RGB value." in {
      Green.extract(color.toRGB) shouldBe 100
    }
  }

  "Blue channel" can {
    "be extracted from an arbitrary RGB value." in {
      Blue.extract(color.toRGB) shouldBe 150
    }
  }

  "Alpha channel" can {
    "be extracted from an arbitrary RGB value." in {
      Alpha.extract(color.toRGB) shouldBe 200
    }
  }
}
