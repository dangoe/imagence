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

import de.dangoe.imatch.ImageSlicing._
import Testhelpers._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
class PercentageSlicingTest extends WordSpec with Matchers {

  val quadraticImage = readImage("quadratic.png")
  val rectangularImage = readImage("rectangular.png")
  val rectangularWithOddEdgeLengthsImage = readImage("rectangular_with_odd_edge_lengths.png")

  "Percentage slicing" should {
    "slice an quadratic image in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        val slices = quadraticImage.slice(new PercentageSlicing(0.5))

        slices.length shouldBe 4
        slices.head.image should showTheSameAs(readImage("quadratic_11.png"))
        slices.head.anchor shouldBe(0, 0)
        slices(1).image should showTheSameAs(readImage("quadratic_12.png"))
        slices(1).anchor shouldBe(16, 0)
        slices(2).image should showTheSameAs(readImage("quadratic_21.png"))
        slices(2).anchor shouldBe(0, 16)
        slices(3).image should showTheSameAs(readImage("quadratic_22.png"))
        slices(3).anchor shouldBe(16, 16)
      }
    }

    "slice an rectangular image with even edge lengths in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        val slices = rectangularImage.slice(new PercentageSlicing(0.5))

        slices.length shouldBe 4
        slices.head.image should showTheSameAs(readImage("rectangular_11.png"))
        slices.head.anchor shouldBe(0, 0)
        slices(1).image should showTheSameAs(readImage("rectangular_12.png"))
        slices(1).anchor shouldBe(64, 0)
        slices(2).image should showTheSameAs(readImage("rectangular_21.png"))
        slices(2).anchor shouldBe(0, 32)
        slices(3).image should showTheSameAs(readImage("rectangular_22.png"))
        slices(3).anchor shouldBe(64, 32)
      }
    }

    "slice an rectangular image with odd edge lengths in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        val slices = rectangularWithOddEdgeLengthsImage.slice(new PercentageSlicing(0.5))

        slices.length shouldBe 4
        slices.head.image should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_11.png"))
        slices.head.anchor shouldBe(0, 0)
        slices(1).image should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_12.png"))
        slices(1).anchor shouldBe(64, 0)
        slices(2).image should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_21.png"))
        slices(2).anchor shouldBe(0, 32)
        slices(3).image should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_22.png"))
        slices(3).anchor shouldBe(64, 32)
      }
    }
  }

  it must {
    "not produce slices with an edge length shorter than four pixels" when {
      "calculated edge length is shorter than this value." in {
        val slices = rectangularImage.slice(new PercentageSlicing(0.01))

        val firstSlice = slices.head
        firstSlice.image.getWidth shouldBe 4
        firstSlice.image.getWidth shouldBe 4
      }
    }
  }
}
