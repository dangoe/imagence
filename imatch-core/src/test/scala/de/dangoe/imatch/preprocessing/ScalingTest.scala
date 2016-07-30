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
package de.dangoe.imatch.preprocessing

import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}

import de.dangoe.imatch.matching.Dimension
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
class ScalingTest extends WordSpec with Matchers {

  val bounds = Dimension(40, 40)

  val landscapeFormatImage = new BufferedImage(160, 120, BufferedImage.TYPE_BYTE_GRAY)
  val portraitFormatImage = new BufferedImage(120, 160, BufferedImage.TYPE_BYTE_GRAY)
  val squareFormatImage = new BufferedImage(120, 120, BufferedImage.TYPE_BYTE_GRAY)

  "Scaling" should {
    "scale an image to defined width" when {
      "to width scaling is used" when {
        "image is in landscape format." in {
          val resized = new Scaling(bounds, ToWidth).apply(landscapeFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 30
        }

        "image is in portrait format." in {
          val resized = new Scaling(bounds, ToWidth).apply(portraitFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 53
        }

        "image is in square format." in {
          val resized = new Scaling(bounds, ToWidth).apply(squareFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 40
        }
      }
    }

    "scale an image to defined height" when {
      "to height scaling is used" when {
        "image is in landscape format." in {
          val resized = new Scaling(bounds, ToHeight).apply(landscapeFormatImage)

          resized.getWidth shouldBe 53
          resized.getHeight shouldBe 40
        }

        "image is in portrait format." in {
          val resized = new Scaling(bounds, ToHeight).apply(portraitFormatImage)

          resized.getWidth shouldBe 30
          resized.getHeight shouldBe 40
        }

        "image is in square format." in {
          val resized = new Scaling(bounds, ToHeight).apply(squareFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 40
        }
      }
    }

    "scale an image to defined bounding box" when {
      "to bounding box scaling is used" when {
        "image is in landscape format." in {
          val resized = new Scaling(bounds, ToBoundingBox).apply(landscapeFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 30
        }

        "image is in portrait format." in {
          val resized = new Scaling(bounds, ToBoundingBox).apply(portraitFormatImage)

          resized.getWidth shouldBe 30
          resized.getHeight shouldBe 40
        }

        "image is in square format." in {
          val resized = new Scaling(bounds, ToBoundingBox).apply(squareFormatImage)

          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 40
        }
      }
    }

    "preserve the image contents." in {
      val graphics = squareFormatImage.getGraphics.asInstanceOf[Graphics2D]
      graphics.setColor(Color.WHITE)
      graphics.fillRect(0, 0, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
      graphics.setColor(Color.BLACK)
      graphics.fillRect(0, squareFormatImage.getHeight / 2 + 1, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
      graphics.dispose()

      val expectedImage = new BufferedImage(40, 40, BufferedImage.TYPE_BYTE_GRAY)
      graphics.setColor(Color.WHITE)
      graphics.fillRect(0, 0, expectedImage.getWidth, expectedImage.getHeight / 2)
      graphics.setColor(Color.BLACK)
      graphics.fillRect(0, expectedImage.getHeight / 2 + 1, expectedImage.getWidth, expectedImage.getHeight / 2)
      graphics.dispose()

      val resized = new Scaling(bounds, ToBoundingBox).apply(squareFormatImage)

      resized.getRGB(0, 0) shouldBe Color.WHITE.getRGB
      resized.getRGB(0, 39) shouldBe Color.BLACK.getRGB
    }
  }
}
