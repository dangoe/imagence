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

import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}

import de.dangoe.imagence.api.matching.Dimension
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
class ScalingTest extends WordSpec with Matchers with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  val downscalingBounds = Dimension(40, 40)
  val upscalingBounds = Dimension(200, 200)

  val landscapeFormatImage = new BufferedImage(160, 120, BufferedImage.TYPE_BYTE_GRAY)
  val portraitFormatImage = new BufferedImage(120, 160, BufferedImage.TYPE_BYTE_GRAY)
  val squareFormatImage = new BufferedImage(120, 120, BufferedImage.TYPE_BYTE_GRAY)

  override implicit def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(25, Millis))

  "Scaling" should {
    "use to bounding box as default strategy" when {
      "image is in landscape format." in {
        whenReady(Scaling.toBoundingBox(downscalingBounds).apply(landscapeFormatImage)) { resized =>
          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 30
        }
      }

      "image is in portrait format." in {
        whenReady(Scaling.toBoundingBox(downscalingBounds).apply(portraitFormatImage)) { resized =>
          resized.getWidth shouldBe 30
          resized.getHeight shouldBe 40
        }
      }

      "image is in square format." in {
        whenReady(Scaling.toBoundingBox(downscalingBounds).apply(squareFormatImage)) { resized =>
          resized.getWidth shouldBe 40
          resized.getHeight shouldBe 40
        }
      }
    }

    "scale an image down to defined width" when {
      "to width scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(downscalingBounds, ToWidth).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 30
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(downscalingBounds, ToWidth).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 53
          }
        }

        "image is in square format." in {
          whenReady(Scaling(downscalingBounds, ToWidth).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }
      }
    }

    "scale an image down to defined height" when {
      "to height scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(downscalingBounds, ToHeight).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 53
            resized.getHeight shouldBe 40
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(downscalingBounds, ToHeight).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 30
            resized.getHeight shouldBe 40
          }
        }

        "image is in square format." in {
          whenReady(Scaling(downscalingBounds, ToHeight).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }
      }
    }

    "scale an image down to defined bounding box" when {
      "to bounding box scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(downscalingBounds, ToBoundingBox).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 30
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(downscalingBounds, ToBoundingBox).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 30
            resized.getHeight shouldBe 40
          }
        }

        "image is in square format." in {
          whenReady(Scaling(downscalingBounds, ToBoundingBox).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }
      }
    }

    "scale an image down to defined size" when {
      "exact scaling without maintaining its aspect ratio is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(downscalingBounds, Exact).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(downscalingBounds, Exact).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }

        "image is in square format." in {
          whenReady(Scaling(downscalingBounds, Exact).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 40
            resized.getHeight shouldBe 40
          }
        }
      }
    }

    "scale an image up to defined width" when {
      "to width scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(upscalingBounds, ToWidth).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 150
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(upscalingBounds, ToWidth).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 266
          }
        }

        "image is in square format." in {
          whenReady(Scaling(upscalingBounds, ToWidth).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }
      }
    }

    "scale an image up to defined height" when {
      "to height scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(upscalingBounds, ToHeight).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 266
            resized.getHeight shouldBe 200
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(upscalingBounds, ToHeight).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 150
            resized.getHeight shouldBe 200
          }
        }

        "image is in square format." in {
          whenReady(Scaling(upscalingBounds, ToHeight).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }
      }
    }

    "scale an image up to defined bounding box" when {
      "to bounding box scaling is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(upscalingBounds, ToBoundingBox).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 150
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(upscalingBounds, ToBoundingBox).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 150
            resized.getHeight shouldBe 200
          }
        }

        "image is in square format." in {
          whenReady(Scaling(upscalingBounds, ToBoundingBox).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }
      }
    }

    "scale an image up to defined size" when {
      "exact scaling without maintaining its aspect ratio is used" when {
        "image is in landscape format." in {
          whenReady(Scaling(upscalingBounds, Exact).apply(landscapeFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }

        "image is in portrait format." in {
          whenReady(Scaling(upscalingBounds, Exact).apply(portraitFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }

        "image is in square format." in {
          whenReady(Scaling(upscalingBounds, Exact).apply(squareFormatImage)) { resized =>
            resized.getWidth shouldBe 200
            resized.getHeight shouldBe 200
          }
        }
      }
    }

    "preserve the image contents" when {
      "downscaled." in {
        val graphics = squareFormatImage.getGraphics.asInstanceOf[Graphics2D]
        graphics.setColor(Color.WHITE)
        graphics.fillRect(0, 0, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
        graphics.setColor(Color.BLACK)
        graphics.fillRect(0, squareFormatImage.getHeight / 2 + 1, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
        graphics.dispose()

        val expectedImage = new BufferedImage(40, 40, BufferedImage.TYPE_BYTE_GRAY)
        graphics.setColor(Color.WHITE)
        graphics.fillRect(0, 0, expectedImage.getWidth, expectedImage.getHeight / 2)
        graphics.setColor(Color.BLACK)
        graphics.fillRect(0, expectedImage.getHeight / 2 + 1, expectedImage.getWidth, expectedImage.getHeight / 2)
        graphics.dispose()

        whenReady(Scaling(downscalingBounds, ToBoundingBox).apply(squareFormatImage)) { resized =>
          resized.getRGB(0, 0) shouldBe Color.WHITE.getRGB
          resized.getRGB(0, 39) shouldBe Color.BLACK.getRGB
        }
      }

      "upscaled." in {
        val graphics = squareFormatImage.getGraphics.asInstanceOf[Graphics2D]
        graphics.setColor(Color.WHITE)
        graphics.fillRect(0, 0, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
        graphics.setColor(Color.BLACK)
        graphics.fillRect(0, squareFormatImage.getHeight / 2 + 1, squareFormatImage.getWidth, squareFormatImage.getHeight / 2)
        graphics.dispose()

        val expectedImage = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY)
        graphics.setColor(Color.WHITE)
        graphics.fillRect(0, 0, expectedImage.getWidth, expectedImage.getHeight / 2)
        graphics.setColor(Color.BLACK)
        graphics.fillRect(0, expectedImage.getHeight / 2 + 1, expectedImage.getWidth, expectedImage.getHeight / 2)
        graphics.dispose()

        whenReady(Scaling(upscalingBounds, ToBoundingBox).apply(squareFormatImage)) { resized =>
          resized.getRGB(0, 0) shouldBe Color.WHITE.getRGB
          resized.getRGB(0, 199) shouldBe Color.BLACK.getRGB
        }
      }
    }
  }
}
