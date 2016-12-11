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
package de.dangoe.imagence.testsupport

import java.awt.Color
import java.awt.image.BufferedImage

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 16.08.16
  */
class ImageFactoryTest extends WordSpec with Matchers with MockFactory with ImageFactory {

  "ImageFactory" should {
    "allow to create an image of a given size." in {
      val image = createImage(OnePixel, stub[DrawingStrategy], BufferedImage.TYPE_INT_ARGB)

      image.dimension shouldBe OnePixel
    }

    "allow to create an image using a given drawing strategy." in {
      val drawingStrategy = mock[DrawingStrategy]
      (drawingStrategy.draw(_: BufferedImage)).expects(*).once()

      val image = createImage(Dimension(20, 10), drawingStrategy, BufferedImage.TYPE_INT_ARGB)
    }

    "allow to create an image of type ARGB." in {
      val image = createImage(OnePixel, stub[DrawingStrategy], BufferedImage.TYPE_INT_ARGB)

      image.getType shouldBe BufferedImage.TYPE_INT_ARGB
    }

    "allow to create an image of type gray." in {
      val image = createImage(OnePixel, stub[DrawingStrategy], BufferedImage.TYPE_BYTE_GRAY)

      image.getType shouldBe BufferedImage.TYPE_BYTE_GRAY
    }
  }

  "Fill" can {
    "be used to create an image with one constant color." in {
      val image = createImage(Dimension(240, 240), Fill(Color.GREEN), BufferedImage.TYPE_INT_ARGB)

      for (x <- 0 until 240;
           y <- 0 until 240) {
        new Color(image.getRGB(x, y)) shouldBe Color.GREEN
      }
    }
  }
}
