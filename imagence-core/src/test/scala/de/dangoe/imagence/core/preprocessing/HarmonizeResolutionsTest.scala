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

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Dimension.square
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
class HarmonizeResolutionsTest extends WordSpec with Matchers with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  val reference = new BufferedImage(800, 600, BufferedImage.TYPE_BYTE_GRAY)
  val smallerImage = new BufferedImage(42, 42, BufferedImage.TYPE_BYTE_GRAY)
  val largerImage = new BufferedImage(4200, 4200, BufferedImage.TYPE_BYTE_GRAY)

  override implicit def patienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(25, Millis))

  "HarmonizeResolutions" should {
    "scale a smaller image to the reference image's size while the reference image remains unchanged." in {
      val sut = HarmonizeResolutions.byScalingToReference()

      whenReady(sut(ProcessingInput(smallerImage, reference))) { processed =>
        processed.image.getWidth shouldBe 800
        processed.image.getHeight shouldBe 600
        processed.reference.getWidth shouldBe 800
        processed.reference.getHeight shouldBe 600
      }
    }

    "scale a larger image to the reference image's size while the reference image remains unchanged." in {
      val sut = HarmonizeResolutions.byScalingToReference()

      whenReady(sut(ProcessingInput(largerImage, reference))) { processed =>
        processed.image.getWidth shouldBe 800
        processed.image.getHeight shouldBe 600
        processed.reference.getWidth shouldBe 800
        processed.reference.getHeight shouldBe 600
      }
    }

    "allow to pass a target bounding box that is used to resize both images to am matching size within these bounds" when {
      "the image is smaller than the reference image." in {
        val sut = HarmonizeResolutions(Scaling.toBoundingBox(square(400)))

        whenReady(sut(ProcessingInput(smallerImage, reference))) { processed =>
          processed.image.getWidth shouldBe 400
          processed.image.getHeight shouldBe 300
          processed.reference.getWidth shouldBe 400
          processed.reference.getHeight shouldBe 300
        }
      }

      "the image is larger than the reference image." in {
        val sut = HarmonizeResolutions(Scaling.toBoundingBox(square(400)))

        whenReady(sut(ProcessingInput(largerImage, reference))) { processed =>
          processed.image.getWidth shouldBe 400
          processed.image.getHeight shouldBe 300
          processed.reference.getWidth shouldBe 400
          processed.reference.getHeight shouldBe 300
        }
      }
    }
  }
}
