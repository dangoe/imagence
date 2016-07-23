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

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class SimpleDifferenceMatchingTest extends WordSpec with Matchers {

  "Simple difference matching" should {
    "calculate a deviation of zero" when {
      "if the image to be compared is equal to the reference image." in {
        val image = readImage("quadratic.png")

        val result = SimpleDifferenceMatching.evaluate(image, image)

        result.deviation shouldBe 0
      }
    }

    "calculate a deviation greater than zero" when {
      "if the image to be compared is not equal to the reference image." in {
        val image = readImage("quadratic_11.png")
        val referenceImage = readImage("quadratic_22.png")

        val result = SimpleDifferenceMatching.evaluate(image, referenceImage)

        result.deviation > 0 shouldBe true
        result.deviantPixelCount shouldBe 84
      }
    }
  }

  private def readImage(resourceName: String): BufferedImage = {
    ImageIO.read(getClass.getResourceAsStream(resourceName))
  }
}
