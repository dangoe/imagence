package de.dangoe.imagence

import java.awt.image.BufferedImage

import de.dangoe.imagence.Implicits.RichBufferedImage
import de.dangoe.imagence.matching.Dimension
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}


/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 03.08.16
  */
class RichBufferedImageTest extends WordSpec with Matchers with MockitoSugar {

  "RichBufferedImage" when {
    "aspectRatio is called" when {
      "image is in landscape format with a width twice the length of the height" should {
        "return 2." in {
          val sut = new RichBufferedImage(mockImage(400, 200))

          sut.aspectRatio shouldBe 2
        }
      }

      "image is in portrait format with a height twice the length of the width" should {
        "return 0.5." in {
          val sut = new RichBufferedImage(mockImage(200, 400))

          sut.aspectRatio shouldBe 0.5
        }
      }

      "image is in square format" should {
        "return a value larger equal to one." in {
          val sut = new RichBufferedImage(mockImage(200, 200))

          sut.aspectRatio shouldBe 1
        }
      }
    }

    "dimension is called" should {
      "return an Dimension instance describing the image's width and height." in {
        val sut = new RichBufferedImage(mockImage(400, 200))

        sut.dimension shouldBe Dimension(400, 200)
      }
    }
  }

  private def mockImage(width: Int, height: Int): BufferedImage = {
    val image = mock[BufferedImage]
    when(image.getWidth).thenReturn(width)
    when(image.getHeight).thenReturn(height)
    image
  }
}
