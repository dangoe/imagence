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
import de.dangoe.imatch.matching.ImplicitConversions._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
class ConvertToGreyscaleTest extends WordSpec with Matchers {

  implicit val executionContext = ExecutionContext.global
  implicit val timeout = 15 seconds

  val color = new Color(50, 100, 150, 200)

  "Any color" can {
    "be converted to greyscale using averaging method." in {
      val image = new GreyscaleConversion(Averaging).apply(createTestImage(Dimension(1, 1), color))
      val backgroundColor = new Color(image.getRGB(0, 0), true)

      image.dimension shouldBe Dimension(1, 1)
      backgroundColor.getRGB shouldBe -6908266
    }

    "be converted to greyscale using desaturation method." in {
      val image = new GreyscaleConversion(Desaturation).apply(createTestImage(Dimension(1, 1), color))
      val backgroundColor = new Color(image.getRGB(0, 0), true)

      image.dimension shouldBe Dimension(1, 1)
      backgroundColor.getRGB shouldBe -6908266
    }

    "be converted to greyscale using luma method." in {
      val image = new GreyscaleConversion(Luma).apply(createTestImage(Dimension(1, 1), color))
      val backgroundColor = new Color(image.getRGB(0, 0), true)

      image.dimension shouldBe Dimension(1, 1)
      backgroundColor.getRGB shouldBe -7303024
    }
  }

  private def createTestImage(dimension: Dimension, backgroundColor: Color): BufferedImage = {
    val image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.getGraphics
    graphics.setColor(backgroundColor)
    graphics.asInstanceOf[Graphics2D].fillRect(0, 0, dimension.width, dimension.height)
    image
  }
}
