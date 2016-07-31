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
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.math.BigDecimal.RoundingMode

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class PixelWiseColorDeviationMatchingTest extends WordSpec with Matchers {

  implicit val executionContext = ExecutionContext.global
  implicit val timeout = 15 seconds

  "PixelWiseColorDeviationMatching" should {
    "calculate a deviation of zero" when {
      "if the image to be compared is equal to the reference image." in {
        val image = readImage("quadratic.png")

        val processingInput = ProcessingInput(image, image)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        result.deviation.value shouldBe 0
      }
    }

    "calculate a deviation greater than zero" when {
      "if the image to be compared is not equal to the reference image." in {
        val image = readImage("quadratic_11.png")
        val referenceImage = readImage("quadratic_22.png")

        val processingInput = ProcessingInput(image, referenceImage)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        BigDecimal.valueOf(result.deviation.value).setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(0.71)
        result.deviantPixelCount shouldBe 169
      }
    }

    "calculate a deviation of one" when {
      "if the image is completely white and the reference image is completely black." in {
        val image = readImage("white.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        result.deviation.value shouldBe 1
        result.deviantPixelCount shouldBe 4096
      }
    }

    "calculate a deviation of one" when {
      "if the image is completely black and the reference image is completely white." in {
        val image = readImage("white.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        result.deviation.value shouldBe 1
        result.deviantPixelCount shouldBe 4096
      }
    }

    "calculate a deviation of one half" when {
      "if the image is completely white and the reference image is completely grey." in {
        val image = readImage("white.png")
        val referenceImage = readImage("grey.png")

        val processingInput = ProcessingInput(image, referenceImage)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        BigDecimal.valueOf(result.deviation.value).setScale(1, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(.5)
        result.deviantPixelCount shouldBe 4096
      }
    }

    "calculate a deviation of one half" when {
      "if the image is completely grey and the reference image is completely white." in {
        val image = readImage("grey.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)
        implicit val context = ImageProcessingContext(processingInput)

        val result = PixelWiseColorDeviationMatching().apply(processingInput)

        BigDecimal.valueOf(result.deviation.value).setScale(1, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(.5)
        result.deviantPixelCount shouldBe 4096
      }
    }
  }
}
