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

import java.awt.Color
import java.awt.image.BufferedImage

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.testsupport._
import org.scalatest.{Matchers, WordSpec}

import scala.math.BigDecimal.RoundingMode

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 31.07.16
  */
class EuclideanDistanceCalculatorTest extends WordSpec with Matchers with ImageFactory {

  val firstColor = new Color(23, 42, 13)
  val secondColor = new Color(13, 23, 42)

  "EuclideanDistance" should {
    "use only one channel" when {
      "image is greyscale." in {
        val image = createImage(OnePixel, Fill(firstColor), BufferedImage.TYPE_BYTE_GRAY)
        val reference = createImage(OnePixel, Fill(secondColor), BufferedImage.TYPE_BYTE_GRAY)

        val deviation = new EuclideanDistanceCalculator(ProcessingInput(image, reference)).calculate(firstColor, secondColor).get.value

        round(deviation) shouldBe round(0.039)
      }
    }

    "use all channels" when {
      "image is not greyscale." in {
        val image = createImage(OnePixel, Fill(firstColor), BufferedImage.TYPE_INT_ARGB)
        val reference = createImage(OnePixel, Fill(secondColor), BufferedImage.TYPE_INT_ARGB)

        val deviation = new EuclideanDistanceCalculator(ProcessingInput(image, reference)).calculate(firstColor, secondColor).get.value

        round(deviation) shouldBe round(0.081)
      }
    }

    "be one" when {
      "black is compared to white." in {
        val image = createImage(OnePixel, Fill(Color.BLACK), BufferedImage.TYPE_BYTE_GRAY)
        val reference = createImage(OnePixel, Fill(Color.WHITE), BufferedImage.TYPE_BYTE_GRAY)

        val deviation = new EuclideanDistanceCalculator(ProcessingInput(image, reference)).calculate(Color.BLACK, Color.WHITE).get.value

        round(deviation) shouldBe round(1)
      }
    }

    "be 0.5" when {
      "grey is compared to white." in {
        val image = createImage(OnePixel, Fill(Color.GRAY), BufferedImage.TYPE_BYTE_GRAY)
        val reference = createImage(OnePixel, Fill(Color.WHITE), BufferedImage.TYPE_BYTE_GRAY)

        val deviation = new EuclideanDistanceCalculator(ProcessingInput(image, reference)).calculate(Color.GRAY, Color.WHITE).get.value

        round(deviation) shouldBe round(0.5)
      }
    }

    "be 0.5" when {
      "grey is compared to black." in {
        val image = createImage(OnePixel, Fill(Color.GRAY), BufferedImage.TYPE_BYTE_GRAY)
        val reference = createImage(OnePixel, Fill(Color.BLACK), BufferedImage.TYPE_BYTE_GRAY)

        val deviation = new EuclideanDistanceCalculator(ProcessingInput(image, reference)).calculate(Color.GRAY, Color.BLACK).get.value

        round(deviation) shouldBe round(0.5)
      }
    }
  }

  private def round(deviation: Double) = BigDecimal.valueOf(deviation).setScale(2, RoundingMode.HALF_UP)

  private implicit def colorToRgb(color: Color): Int = color.getRGB
}
