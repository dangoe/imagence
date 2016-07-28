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

import de.dangoe.imatch.Testhelpers._
import de.dangoe.imatch.common.ImageProcessingContext
import de.dangoe.imatch.matching.Anchor._
import de.dangoe.imatch.matching.Deviation.NoDeviation
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class MatchingStrategyTest extends WordSpec with Matchers {

  val sut = new MatchingStrategy[MatchingResult] {
    override protected def applyInternal(image: Slice, reference: Slice): MatchingResult = new MatchingResult {
      override def context: ImageProcessingContext = ImageProcessingContext(image, reference)
      override def deviation: Deviation = NoDeviation
      override def region: Region = Region(PointOfOrigin, Dimension(image.getWidth, image.getHeight))
    }
  }

  "Any matching strategy" must {
    "throw an ImageMatchingException" when {
      "image size differs from reference image size." in {
        val quadraticImage = readImage("quadratic.png")
        val rectangularImage = readImage("rectangular.png")

        implicit val context = ImageProcessingContext(quadraticImage, rectangularImage)

        intercept[ImageMatchingException] {
          sut(quadraticImage, rectangularImage)
        }
      }
    }
  }
}
