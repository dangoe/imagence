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
package de.dangoe.imagence.core.preprocessing

import java.awt.Color

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.testsupport._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 17.08.16
  */
class GaussianBlurTest extends WordSpec with Matchers with MockitoSugar with ImageReader with ImageFactory {

  private final val ImageDimension = Dimension(42, 23)

  private final val WhiteImage = createImage(ImageDimension, Fill(Color.WHITE))
  private final val BlackImage = createImage(ImageDimension, Fill(Color.BLACK))

  "Gaussian blur" should {
    "create an image of same size." in {
      val blurredImage = GaussianBlur(1).apply(createImage(ImageDimension, mock[DrawingStrategy]))

      blurredImage.dimension shouldBe ImageDimension
    }

    "create a white image" when {
      "the input image is white." in {
        val blurredImage = GaussianBlur(1).apply(WhiteImage)

        blurredImage should showTheSameAs(WhiteImage)
      }
    }

    "create a black image" when {
      "the input image is black." in {
        val blurredImage = GaussianBlur(1).apply(BlackImage)

        blurredImage should showTheSameAs(BlackImage)
      }
    }

    "create a blurred version" when {
      "filter radius is 1 pixel." in {
        val blurredImage = GaussianBlur(1).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r1.png"))
      }

      "filter radius is 2 pixels." in {
        val blurredImage = GaussianBlur(2).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r2.png"))
      }

      "filter radius is 3 pixels." in {
        val blurredImage = GaussianBlur(3).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r3.png"))
      }

      "filter radius is 5 pixels." in {
        val blurredImage = GaussianBlur(5).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r5.png"))
      }

      "filter radius is 7 pixels." in {
        val blurredImage = GaussianBlur(7).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r7.png"))
      }

      "filter radius is 11 pixels." in {
        val blurredImage = GaussianBlur(11).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r11.png"))
      }

      "filter radius is 29 pixels." in {
        val blurredImage = GaussianBlur(29).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r29.png"))
      }

      "filter radius is 47 pixels." in {
        val blurredImage = GaussianBlur(47).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r47.png"))
      }

      "filter radius is 71 pixels." in {
        val blurredImage = GaussianBlur(71).apply(readImage("pattern.png"))

        blurredImage should showTheSameAs(readImage("pattern_blurred_r71.png"))
      }
    }
  }
}
