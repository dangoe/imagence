package de.dangoe.imatch.common

import java.awt.image.BufferedImage

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 29.07.16
  */
class ImageProcessingContextTest extends WordSpec with Matchers {

  "ImageProcessingContext" can {
    "not be created" when {
      "image type and reference image type differ." in {
        intercept[IllegalArgumentException] {
          ImageProcessingContext(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))
        }
      }
    }
  }

  it should {
    "determine greyscale as color model" when {
      "reference image type is TYPE_BYTE_GRAY." in {
        val sut = ImageProcessingContext(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY))

        sut.colorModel shouldBe Greyscale
      }
    }

    "determine RGB as color model" when {
      "reference image type is not TYPE_BYTE_GRAY." in {
        val sut = ImageProcessingContext(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))

        sut.colorModel shouldBe RGB
      }
    }
  }
}
