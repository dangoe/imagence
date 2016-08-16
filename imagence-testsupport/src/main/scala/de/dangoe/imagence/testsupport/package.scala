package de.dangoe.imagence

import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}
import javax.imageio.ImageIO

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import org.scalatest.matchers.{MatchResult, Matcher}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 16.08.16
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

  def createOneColoredImage(dimension: Dimension, backgroundColor: Color, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage = {
    val image = new BufferedImage(dimension.width, dimension.height, imageType)
    val graphics = image.getGraphics
    graphics.setColor(backgroundColor)
    graphics.asInstanceOf[Graphics2D].fillRect(0, 0, dimension.width, dimension.height)
    image
  }
}
