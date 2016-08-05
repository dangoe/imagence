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
package de.dangoe.imagence.api.matching

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
class DimensionTest extends WordSpec with Matchers {

  "A dimension" can {
    "be created" when {
      "both dimensions larger than zero." in {
        val dimension = Dimension(1, 1)

        dimension.width shouldBe 1
        dimension.height shouldBe 1
      }
    }

    "not be created" when {
      "horizontal dimension is zero." in {
        intercept[IllegalArgumentException] {
          Dimension(1, 0)
        }
      }

      "vertical dimension is zero." in {
        intercept[IllegalArgumentException] {
          Dimension(0, 1)
        }
      }

      "horizontal dimension is negative." in {
        intercept[IllegalArgumentException] {
          Dimension(-1, 1)
        }
      }

      "vertical dimension is negative." in {
        intercept[IllegalArgumentException] {
          Dimension(1, -1)
        }
      }
    }
  }

  "A square dimension" can {
    "be created for a given edge length." in {
      val dimension = Dimension.square(2)

      dimension shouldBe Dimension(2, 2)
    }
  }
}
