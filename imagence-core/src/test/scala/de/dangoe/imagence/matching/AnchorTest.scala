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
class AnchorTest extends WordSpec with Matchers {

  "An anchor" can {
    "be created" when {
      "both coordinates are zero." in {
        val anchor = Anchor(0, 0)

        anchor.x shouldBe 0
        anchor.y shouldBe 0
      }

      "horizontal coordinate is zero." in {
        val anchor = Anchor(0, 1)

        anchor.x shouldBe 0
        anchor.y shouldBe 1
      }

      "vertical coordinate is zero." in {
        val anchor = Anchor(1, 0)

        anchor.x shouldBe 1
        anchor.y shouldBe 0
      }

      "both coordinates larger than zero." in {
        val anchor = Anchor(1, 1)

        anchor.x shouldBe 1
        anchor.y shouldBe 1
      }
    }

    "not be created" when {
      "horizontal coordinate is negative." in {
        intercept[IllegalArgumentException] {
          Anchor(-1, 0)
        }
      }

      "vertical coordinate is negative." in {
        intercept[IllegalArgumentException] {
          Anchor(0, -1)
        }
      }
    }
  }
}
