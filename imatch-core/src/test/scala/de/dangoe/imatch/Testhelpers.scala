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

import de.dangoe.imatch.matching.ImplicitConversions._
import org.scalatest.matchers.{MatchResult, Matcher}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
object Testhelpers {

  case class ImageShowsTheSameMatcher(image: BufferedImage) extends Matcher[BufferedImage] {

    def apply(otherImage: BufferedImage): MatchResult = {
      MatchResult(otherImage.isOfSameSizeAs(image) && sameRGBValuesPerPixel(otherImage),
        "Images are not the same!",
        "Images are not the same!")
    }

    private def sameRGBValuesPerPixel(otherImage: BufferedImage): Boolean = {
      (for (x <- 0 until otherImage.getWidth;
            y <- 0 until otherImage.getHeight
      ) yield (x, y)).forall(p => image.getRGB(p._1, p._2) == otherImage.getRGB(p._1, p._2))
    }
  }

  def showTheSameAs(image: BufferedImage) = ImageShowsTheSameMatcher(image)

  def readImage(resourceName: String): BufferedImage = ImageIO.read(getClass.getResourceAsStream(resourceName))
}
