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
package de.dangoe.imagence

import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}
import javax.imageio.ImageIO

import de.dangoe.imagence.Implicits._
import de.dangoe.imagence.matching.Dimension
import org.scalatest.matchers.{MatchResult, Matcher}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 04.08.16
  */
package object testsupport {

  trait ImageReader {
    def readImage(imageResourceName: String): BufferedImage = ImageIO.read(getClass.getResourceAsStream(imageResourceName))
  }

  case class ImageShowsTheSameMatcher(image: BufferedImage) extends Matcher[BufferedImage] {

    def apply(otherImage: BufferedImage): MatchResult =  {
      MatchResult(otherImage.dimension == image.dimension && sameRGBValuesPerPixel(otherImage),
        "Images are different!",
        "Images are the same but shouldn't have been!")
    }

    private def sameRGBValuesPerPixel(otherImage: BufferedImage): Boolean = {
      (for (x <- 0 until otherImage.getWidth;
            y <- 0 until otherImage.getHeight
            if image.getRGB(x, y) != otherImage.getRGB(x, y))
        yield (x, y)).isEmpty
    }
  }

  def showTheSameAs(image: BufferedImage) = ImageShowsTheSameMatcher(image)

  def createImage(dimension: Dimension, backgroundColor: Color, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage = {
    val image = new BufferedImage(dimension.width, dimension.height, imageType)
    val graphics = image.getGraphics
    graphics.setColor(backgroundColor)
    graphics.asInstanceOf[Graphics2D].fillRect(0, 0, dimension.width, dimension.height)
    image
  }
}
