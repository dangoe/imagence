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
package de.dangoe.imatch.preprocessing

import java.awt.image.BufferedImage

import de.dangoe.imatch.common.{ImageProcessingContext, ProcessingInput}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
class HarmonizeResolutionsTest extends WordSpec with Matchers {

  implicit val executionContext = ExecutionContext.global
  implicit val timeout = 15 seconds

  val reference = new BufferedImage(800, 600, BufferedImage.TYPE_BYTE_GRAY)
  val smallerImage = new BufferedImage(42, 42, BufferedImage.TYPE_BYTE_GRAY)
  val largerImage = new BufferedImage(4200, 4200, BufferedImage.TYPE_BYTE_GRAY)

  "HarmonizeResolutions" should {
    "scale a smaller image to the reference image's size while the reference image remains unchanged." in {
      implicit val context = ImageProcessingContext(ProcessingInput(smallerImage, reference))

      val processed = HarmonizeResolutions().apply(context.processingInput)

      processed.image.getWidth shouldBe 800
      processed.image.getHeight shouldBe 600
      processed.reference.getWidth shouldBe 800
      processed.reference.getHeight shouldBe 600
    }

    "scale a larger image to the reference image's size while the reference image remains unchanged." in {
      implicit val context = ImageProcessingContext(ProcessingInput(largerImage, reference))

      val processed = HarmonizeResolutions().apply(context.processingInput)

      processed.image.getWidth shouldBe 800
      processed.image.getHeight shouldBe 600
      processed.reference.getWidth shouldBe 800
      processed.reference.getHeight shouldBe 600
    }
  }
}
