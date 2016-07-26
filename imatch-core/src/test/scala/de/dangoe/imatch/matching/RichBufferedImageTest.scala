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
package de.dangoe.imatch.matching

import java.awt.Graphics2D
import java.awt.image.BufferedImage

import de.dangoe.imatch.Testhelpers._
import de.dangoe.imatch.common.Colors.Color
import de.dangoe.imatch.common.Colors.ImplicitConversions._
import de.dangoe.imatch.matching.ImplicitConversions._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 19.07.16
  */
class RichBufferedImageTest extends WordSpec with Matchers {

  private val quadraticImage = readImage("quadratic.png")
  private val rectangularImage = readImage("rectangular.png")

  "Aspect ratio calculation" should {
    "return 1" when {
      "the aspect ratio is quadratic." in {
        quadraticImage.aspectRatio shouldBe 1
      }
    }

    "return 2" when {
      "the width equals 2 * height." in {
        rectangularImage.aspectRatio shouldBe 2
      }
    }
  }

  "A RichBufferedImage" should {
    "return an array containing all pixel colors" when {
      val image = createTestImage
      image.getPixels shouldBe {
        Array(
          Array(Color(255, 0, 0, 255), Color(255, 255, 255, 255)),
          Array(Color(255, 255, 0, 255), Color(255, 255, 255, 50))
        )
      }
    }
  }

  private def createTestImage: BufferedImage = {
    val image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.getGraphics.asInstanceOf[Graphics2D]
    graphics.setColor(Color(255, 0, 0, 255).asJava)
    graphics.fillRect(0, 0, 1, 1)
    graphics.setColor(Color(255, 255, 0, 255).asJava)
    graphics.fillRect(1, 0, 1, 1)
    graphics.setColor(Color(255, 255, 255, 255).asJava)
    graphics.fillRect(0, 1, 1, 1)
    graphics.setColor(Color(255, 255, 255, 50).asJava)
    graphics.fillRect(1, 1, 1, 1)
    image
  }
}
