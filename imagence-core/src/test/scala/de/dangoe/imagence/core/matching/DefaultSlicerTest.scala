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
package de.dangoe.imagence.core.matching

import java.awt.image.BufferedImage

import de.dangoe.imagence.api.matching.Anchor._
import de.dangoe.imagence.api.matching.{Anchor, Slice}
import de.dangoe.imagence.core.matching.RelativeSliceSize.OneHalf
import de.dangoe.imagence.testsupport._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
class DefaultSlicerTest extends WordSpec with Matchers with ScalaFutures with ImageReader {

  import DefaultSlicerTest._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val quadraticImage = readImage("quadratic.png")
  private val rectangularImage = readImage("rectangular.png")
  private val rectangularWithOddEdgeLengthsImage = readImage("rectangular_with_odd_edge_lengths.png")

  override implicit def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(25, Millis))

  "Default slicing with percentage slice size" should {
    "slice an quadratic image in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        whenReady(Future.sequence(DefaultSlicer(RelativeSliceSize(OneHalf)).slice(quadraticImage))) { slices =>
          slices.length shouldBe 4
          slices.sliceAt(PointOfOrigin) should showTheSameAs(readImage("quadratic_11.png"))
          slices.sliceAt(Anchor(16, 0)) should showTheSameAs(readImage("quadratic_12.png"))
          slices.sliceAt(Anchor(0, 16)) should showTheSameAs(readImage("quadratic_21.png"))
          slices.sliceAt(Anchor(16, 16)) should showTheSameAs(readImage("quadratic_22.png"))
        }
      }
    }

    "slice an rectangular image with even edge lengths in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        whenReady(Future.sequence(DefaultSlicer(RelativeSliceSize(OneHalf)).slice(rectangularImage))) { slices =>
          slices.length shouldBe 4
          slices.sliceAt(PointOfOrigin) should showTheSameAs(readImage("rectangular_11.png"))
          slices.sliceAt(Anchor(64, 0)) should showTheSameAs(readImage("rectangular_12.png"))
          slices.sliceAt(Anchor(0, 32)) should showTheSameAs(readImage("rectangular_21.png"))
          slices.sliceAt(Anchor(64, 32)) should showTheSameAs(readImage("rectangular_22.png"))
        }
      }
    }

    "slice an rectangular image with odd edge lengths in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        whenReady(Future.sequence(DefaultSlicer(RelativeSliceSize(OneHalf)).slice(rectangularWithOddEdgeLengthsImage))) { slices =>
          slices.length shouldBe 4
          slices.sliceAt(PointOfOrigin) should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_11.png"))
          slices.sliceAt(Anchor(64, 0)) should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_12.png"))
          slices.sliceAt(Anchor(0, 32)) should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_21.png"))
          slices.sliceAt(Anchor(64, 32)) should showTheSameAs(readImage("rectangular_with_odd_edge_lengths_22.png"))
        }
      }
    }
  }
}

object DefaultSlicerTest {

  private implicit class SliceSequence(delegate: Seq[Slice]) {
    def sliceAt(anchor: Anchor): BufferedImage = delegate.find(_.region.anchor == anchor).get.image
  }
}
