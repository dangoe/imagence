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
package de.dangoe.imatch.testhelper

import java.awt.image.BufferedImage

import org.scalatest.matchers.{MatchResult, Matcher}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
object Testhelpers {

  case class ImageShowsTheSameMatcher(image: BufferedImage) extends Matcher[BufferedImage] {

    def apply(otherImage: BufferedImage): MatchResult = {
      MatchResult(ofSameSize(otherImage) && sameRGBValuesPerPixel(otherImage),
        "Images are not the same!",
        "Images are not the same!")
    }

    private def ofSameSize(otherImage: BufferedImage): Boolean = {
      image.getWidth == otherImage.getWidth && image.getHeight == otherImage.getHeight
    }

    private def sameRGBValuesPerPixel(otherImage: BufferedImage): Boolean = {
      for (x <- 0 until otherImage.getWidth) {
        for (y <- 0 until otherImage.getHeight) {
          if (image.getRGB(x, y) != otherImage.getRGB(x, y)) {
            return false
          }
        }
      }
      true
    }
  }

  def showTheSameAs(image: BufferedImage) = ImageShowsTheSameMatcher(image)
}
