package de.dangoe.imatch

import java.awt.{Color, Graphics2D}
import java.awt.image.BufferedImage

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 25.07.16
  */
class ConvertToGreyscaleTest extends WordSpec with Matchers {

  val color = new Color(50, 100, 150, 200)

  "Any color" can {
    "be converted to greyscale using averaging method." in {
      val image = new ConvertToGreyscale(Averaging).process(createTestImage(Dimension(1, 1), color))
      val backgroundColor = image.getRGB(0, 0)

      image.dimension shouldBe Dimension(1, 1)
      // TODO Fix rounding problem
      backgroundColor shouldBe -933010589
    }

    "be converted to greyscale using desaturation method." in {
      val image = new ConvertToGreyscale(Desaturation).process(createTestImage(Dimension(1, 1), color))
      val backgroundColor = image.getRGB(0, 0)

      image.dimension shouldBe Dimension(1, 1)
      // TODO Fix rounding problem
      backgroundColor shouldBe -933010589
    }

    "be converted to greyscale using luma method." in {
      val image = new ConvertToGreyscale(Luma).process(createTestImage(Dimension(1, 1), color))
      val backgroundColor = image.getRGB(0, 0)

      image.dimension shouldBe Dimension(1, 1)
      // TODO Fix rounding problem
      backgroundColor shouldBe -933536933
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
