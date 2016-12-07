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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 31.07.16
  */
class NativeGreyscaleConversionTest extends WordSpec with Matchers with ScalaFutures with ImageFactory {

  import scala.concurrent.ExecutionContext.Implicits.global

  "NativeGreyscaleConversion" should {
    "convert a color to greyscale." in {
      whenReady(NativeGreyscaleConversion.apply(createImage(OnePixel, Fill(new Color(120, 255, 160))))) { image =>
        val backgroundColor = new Color(image.getRGB(0, 0), true)

        image.dimension shouldBe Dimension(1, 1)
        backgroundColor.getRGB shouldBe -1710619
      }
    }
  }
}
