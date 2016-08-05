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
package de.dangoe.imagence.api

import java.awt.image.BufferedImage

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 03.08.16
  */
class RichBufferedImageTest extends WordSpec with Matchers with MockitoSugar {

  "RichBufferedImage" when {
    "aspectRatio is called" when {
      "image is in landscape format with a width twice the length of the height" should {
        "return 2." in {
          val sut = new RichBufferedImage(mockImage(400, 200))

          sut.aspectRatio shouldBe 2
        }
      }

      "image is in portrait format with a height twice the length of the width" should {
        "return 0.5." in {
          val sut = new RichBufferedImage(mockImage(200, 400))

          sut.aspectRatio shouldBe 0.5
        }
      }

      "image is in square format" should {
        "return a value larger equal to one." in {
          val sut = new RichBufferedImage(mockImage(200, 200))

          sut.aspectRatio shouldBe 1
        }
      }
    }

    "dimension is called" should {
      "return an Dimension instance describing the image's width and height." in {
        val sut = new RichBufferedImage(mockImage(400, 200))

        sut.dimension shouldBe Dimension(400, 200)
      }
    }
  }

  private def mockImage(width: Int, height: Int): BufferedImage = {
    val image = mock[BufferedImage]
    when(image.getWidth).thenReturn(width)
    when(image.getHeight).thenReturn(height)
    image
  }
}
