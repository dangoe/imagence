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

  final val OnePixel = Dimension(1, 1)

  trait ImageReader {
    def readImage(imageResourceName: String): BufferedImage = ImageIO.read(getClass.getResourceAsStream(imageResourceName))
  }

  trait DrawingStrategy {
    def draw(image:BufferedImage):Unit
  }

  case class Fill(color: Color) extends DrawingStrategy{
    override def draw(image:BufferedImage): Unit = {
      val graphics  = image.getGraphics.asInstanceOf[Graphics2D]
      graphics.setColor(color)
      graphics.asInstanceOf[Graphics2D].fillRect(0, 0, image.getWidth, image.getHeight)
    }
  }

  trait ImageFactory {
    def createImage(dimension: Dimension, drawingStrategy: DrawingStrategy, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage = {
      val image = new BufferedImage(dimension.width, dimension.height, imageType)
      drawingStrategy.draw(image)
      image
    }
  }

  case class ImageShowsTheSameMatcher(image: BufferedImage) extends Matcher[BufferedImage] {

    def apply(otherImage: BufferedImage): MatchResult =  {
      MatchResult(otherImage.dimension == image.dimension && sameRGBValuesPerPixel(otherImage),
        "Images are different!",
        "Images are the same but shouldn't have been!")
    }

    private def sameRGBValuesPerPixel(otherImage: BufferedImage): Boolean = {
      for (x <- 0 until otherImage.getWidth;
           y <- 0 until otherImage.getHeight) {
        if (image.getRGB(x, y) != otherImage.getRGB(x, y)) return false
      }
      true
    }
  }

  def showTheSameAs(image: BufferedImage) = ImageShowsTheSameMatcher(image)
}
