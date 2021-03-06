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

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Deviation.{MaximumDeviation, NoDeviation}
import de.dangoe.imagence.api.matching._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 02.08.16
  */
class RegionalImageMatcherResultTest extends WordSpec with Matchers {

  case class TestMatchingResult(deviationValue: Double) extends MatchingResult {
    override def deviation: Deviation = Deviation(deviationValue)
  }

  "RegionalImageMatcherResult" should {
    "calculate a deviation of zero" when {
      "no RegionalMatchingResults exist." in {
        val processingInput = ProcessingInput(emptyImage(10, 10), emptyImage(10, 10))
        val regionalMatchingResults = Nil

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe NoDeviation
      }

      "no RegionalMatchingResults with a deviation larger than zero exist." in {
        val processingInput = ProcessingInput(emptyImage(10, 10), emptyImage(10, 10))
        val regionalMatchingResults = Seq(RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(10, 10)), TestMatchingResult(0)))

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe NoDeviation
      }
    }

    "calculate a deviation of one" when {
      "RegionalMatchingResults with a deviation of one exist for the whole image." in {
        val processingInput = ProcessingInput(emptyImage(10, 10), emptyImage(10, 10))
        val regionalMatchingResults = Seq(RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(10, 10)), TestMatchingResult(1)))

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe MaximumDeviation
      }
    }


    "calculate a deviation of one half" when {
      "RegionalMatchingResults with a deviation of one exist for one half of the image." in {
        val processingInput = ProcessingInput(emptyImage(10, 10), emptyImage(10, 10))
        val regionalMatchingResults = Seq(
          RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(5, 5)), TestMatchingResult(1)),
          RegionalMatchingResult(Region(Anchor(5, 5), Dimension(5, 5)), TestMatchingResult(1))
        )

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe Deviation(0.5)
      }
    }
  }

  private def emptyImage(width: Int, height: Int): BufferedImage =
    new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
}
