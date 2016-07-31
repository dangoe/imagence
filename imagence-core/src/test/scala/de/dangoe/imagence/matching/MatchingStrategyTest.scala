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

import de.dangoe.imagence.Testhelpers._
import de.dangoe.imagence.common.{ImageProcessingContext, ProcessingInput}
import de.dangoe.imagence.matching.Anchor._
import de.dangoe.imagence.matching.Deviation.NoDeviation
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class MatchingStrategyTest extends WordSpec with Matchers {

  val sut = new MatchingStrategy[MatchingResult] {
    override protected def applyInternal(input: ProcessingInput): MatchingResult = new MatchingResult {
      override def context: ImageProcessingContext = ImageProcessingContext(input)
      override def deviation: Deviation = NoDeviation
    }
  }

  "Any matching strategy" must {
    "throw an ImageMatchingException" when {
      "image size differs from reference image size." in {
        val quadraticImage = readImage("quadratic.png")
        val rectangularImage = readImage("rectangular.png")

        val processingInput = ProcessingInput(quadraticImage, rectangularImage)
        implicit val context = ImageProcessingContext(processingInput)

        intercept[ImageMatchingException] {
          sut(processingInput)
        }
      }
    }
  }
}