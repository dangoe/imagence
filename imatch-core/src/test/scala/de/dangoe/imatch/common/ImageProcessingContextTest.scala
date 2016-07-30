package de.dangoe.imatch.common

import java.awt.image.BufferedImage

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 29.07.16
  */
class ImageProcessingContextTest extends WordSpec with Matchers {

  "ImageProcessingContext" should {
    "determine greyscale as color model" when {
      "reference image type is TYPE_BYTE_GRAY." in {
        val sut = ImageProcessingContext(ProcessingInput(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY)))

        sut.greyscaleMode shouldBe true
      }
    }

    "determine RGB as color model" when {
      "reference image type is not TYPE_BYTE_GRAY." in {
        val sut = ImageProcessingContext(ProcessingInput(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)))

        sut.greyscaleMode shouldBe false
      }
    }
  }
}
