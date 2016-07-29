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
package de.dangoe.imatch.matching

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 29.07.16
  */
class PercentageSliceSizeTest extends WordSpec with Matchers {

  "PercentageSliceSize" can {
    "be created for a value larger than zero and smaller or equal than one." in {
      val calculation = PercentageSliceSize(0.5)

      calculation.factor shouldBe 0.5
    }
  }

  it must {
    "not accept a percentage value smaller than zero." in {
      intercept[IllegalArgumentException] {
        PercentageSliceSize(-0.1)
      }
    }

    "not accept a percentage value of zero." in {
      intercept[IllegalArgumentException] {
        PercentageSliceSize(0)
      }
    }

    "not accept a percentage value larger than one." in {
      intercept[IllegalArgumentException] {
        PercentageSliceSize(1.1)
      }
    }
  }
}
