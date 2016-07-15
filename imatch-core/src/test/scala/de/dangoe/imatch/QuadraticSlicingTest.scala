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

import de.dangoe.imatch.ImageSlicer._
import de.dangoe.imatch.testhelper.Testhelpers._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
class QuadraticSlicingTest extends WordSpec with Matchers {

  val quadraticImage = readImage("quadratic.png")
  val rectangularImage = readImage("rectangular.png")

  "Quadratic slicing" can {
    "not been applied" when {
      "image is not quadratic." in {
        intercept[IllegalArgumentException] {
          rectangularImage.slice(new QuadraticSlicing(_ => 1))
        }
      }
    }

    "not been applied" when {
      "image is quadratic, but" when {
        "edge length is not dividable by slice edge length." in {
          intercept[IllegalArgumentException] {
            quadraticImage.slice(new QuadraticSlicing(_ => 42))
          }
        }
      }
    }
  }

  it should {
    "slice an quadratic image in 4 slices" when {
      "slice edge length is one-half image edge length." in {
        val slices = quadraticImage.slice(new QuadraticSlicing(_ / 2))

        slices.length shouldBe 4
        slices.head should showTheSameAs(readImage("quadratic_11.png"))
        slices(1) should showTheSameAs(readImage("quadratic_12.png"))
        slices(2) should showTheSameAs(readImage("quadratic_21.png"))
        slices(3) should showTheSameAs(readImage("quadratic_22.png"))
      }
    }
  }

  private def readImage(resourceName: String): BufferedImage = {
    ImageIO.read(getClass.getResourceAsStream(resourceName))
  }
}
