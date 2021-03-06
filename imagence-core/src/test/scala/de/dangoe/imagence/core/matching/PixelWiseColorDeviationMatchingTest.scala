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

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.core.matching.PixelWiseColorDeviationMatching._
import de.dangoe.imagence.testsupport._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.math.BigDecimal.RoundingMode

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class PixelWiseColorDeviationMatchingTest extends WordSpec with Matchers with ScalaFutures with ImageReader {

  import scala.concurrent.ExecutionContext.Implicits.global

  val defaultMatching = PixelWiseColorDeviationMatching(DefaultDeviationCalculatorFactory)

  override implicit def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(25, Millis))

  "PixelWiseColorDeviationMatching" should {
    "calculate a deviation of zero" when {
      "the image to be compared is equal to the reference image." in {
        val image = readImage("quadratic.png")

        val processingInput = ProcessingInput(image, image)

        whenReady(defaultMatching(processingInput)) { result =>
          result.deviation.value shouldBe 0
        }
      }
    }

    "calculate a deviation greater than zero" when {
      "the image to be compared is not equal to the reference image." in {
        val image = readImage("quadratic_11.png")
        val referenceImage = readImage("quadratic_22.png")

        val processingInput = ProcessingInput(image, referenceImage)

        whenReady(defaultMatching(processingInput)) { result =>
          BigDecimal.valueOf(result.deviation.value).setScale(2, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(0.71)
          result.deviantPixelCount shouldBe 169
        }
      }
    }

    "calculate a deviation of one" when {
      "the image is completely white and the reference image is completely black." in {
        val image = readImage("white.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)

        whenReady(defaultMatching(processingInput)) { result =>
          result.deviation.value shouldBe 1
          result.deviantPixelCount shouldBe 4096
        }
      }
    }

    "calculate a deviation of one" when {
      "the image is completely black and the reference image is completely white." in {
        val image = readImage("white.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)

        whenReady(defaultMatching(processingInput)) { result =>
          result.deviation.value shouldBe 1
          result.deviantPixelCount shouldBe 4096
        }
      }
    }

    "calculate a deviation of one half" when {
      "the image is completely white and the reference image is completely grey." in {
        val image = readImage("white.png")
        val referenceImage = readImage("grey.png")

        val processingInput = ProcessingInput(image, referenceImage)

        whenReady(defaultMatching(processingInput)) { result =>
          BigDecimal.valueOf(result.deviation.value).setScale(1, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(.5)
          result.deviantPixelCount shouldBe 4096
        }
      }
    }

    "calculate a deviation of one half" when {
      "the image is completely grey and the reference image is completely white." in {
        val image = readImage("grey.png")
        val referenceImage = readImage("black.png")

        val processingInput = ProcessingInput(image, referenceImage)

        whenReady(defaultMatching(processingInput)) { result =>
          BigDecimal.valueOf(result.deviation.value).setScale(1, RoundingMode.HALF_UP) shouldBe BigDecimal.valueOf(.5)
          result.deviantPixelCount shouldBe 4096
        }
      }
    }
  }
}
