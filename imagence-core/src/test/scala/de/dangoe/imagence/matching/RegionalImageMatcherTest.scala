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
package de.dangoe.imagence.matching

import java.awt.image.BufferedImage

import de.dangoe.imagence.ProcessingInput
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 02.08.16
  */
class RegionalImageMatcherTest extends WordSpec with Matchers with MockitoSugar {

  implicit val executionContext = ExecutionContext.global
  implicit val timeout = 15 seconds

  val slicingStrategy = mock[SlicingStrategy]
  val matchingStrategy = mock[MatchingStrategy[MatchingResult]]

  "A RegionalImageMatcher" must {
    "not accept processing inputs" when {
      "image size differs from reference image size." in {
        intercept[MatchingNotPossible] {
          val input = ProcessingInput(mockImage(320, 240), mockImage(640, 480))

          RegionalImageMatcher(slicingStrategy, matchingStrategy).apply(input)
        }
      }
    }
  }

  it should {
    "slice images using the passed slicing strategy." in {
      when(slicingStrategy.slice(any(classOf[BufferedImage]))).thenReturn(Nil)

      val image = mockImage(320, 240)
      val reference = mockImage(320, 240)

      val input = ProcessingInput(image, reference)

      RegionalImageMatcher(slicingStrategy, matchingStrategy).apply(input)

      verify(slicingStrategy).slice(image)
      verify(slicingStrategy).slice(reference)
    }

    "match all created slice pairs using the passed matching strategy." in {
      val image = mockImage(320, 240)
      val reference = mockImage(320, 240)

      val imageSlice1 = mockSlice(mockImage(160, 240), None)
      val imageSlice2 = mockSlice(mockImage(160, 240), None)
      val referenceSlice1 = mockSlice(mockImage(160, 240), None)
      val referenceSlice2 = mockSlice(mockImage(160, 240), None)

      when(slicingStrategy.slice(image)).thenReturn(Seq(Future(imageSlice1), Future(imageSlice2)))
      when(slicingStrategy.slice(reference)).thenReturn(Seq(Future(referenceSlice1), Future(referenceSlice2)))

      val input = ProcessingInput(image, reference)

      RegionalImageMatcher(slicingStrategy, matchingStrategy).apply(input)

      verify(matchingStrategy).apply(ProcessingInput(imageSlice1.image, referenceSlice1.image))
      verify(matchingStrategy).apply(ProcessingInput(imageSlice2.image, referenceSlice2.image))
    }

    "return the matching results generated by the passed matching strategy." in {
      val image = mockImage(320, 240)
      val reference = mockImage(320, 240)

      val imageSlice1 = mockSlice(mockImage(160, 240), Some(Region(Anchor.PointOfOrigin, Dimension(160, 240))))
      val imageSlice2 = mockSlice(mockImage(160, 240), Some(Region(Anchor(160, 240), Dimension(160, 240))))
      val referenceSlice1 = mockSlice(mockImage(160, 240), Some(Region(Anchor.PointOfOrigin, Dimension(160, 240))))
      val referenceSlice2 = mockSlice(mockImage(160, 240), Some(Region(Anchor(160, 240), Dimension(160, 240))))

      val matchingResult1 = mock[MatchingResult]
      val matchingResult2 = mock[MatchingResult]

      when(slicingStrategy.slice(image)).thenReturn(Seq(Future(imageSlice1), Future(imageSlice2)))
      when(slicingStrategy.slice(reference)).thenReturn(Seq(Future(referenceSlice1), Future(referenceSlice2)))
      when(matchingStrategy.apply(ProcessingInput(imageSlice1.image, referenceSlice1.image))).thenReturn(matchingResult1)
      when(matchingStrategy.apply(ProcessingInput(imageSlice2.image, referenceSlice2.image))).thenReturn(matchingResult2)

      val input = ProcessingInput(image, reference)

      val matchingResult = RegionalImageMatcher(slicingStrategy, matchingStrategy).apply(input)

      matchingResult.processingInput shouldBe input
      matchingResult.regionalMatchingResults.length shouldBe 2
      matchingResult.regionalMatchingResults.head shouldBe RegionalMatchingResult(imageSlice1.region, matchingResult1)
      matchingResult.regionalMatchingResults.last shouldBe RegionalMatchingResult(imageSlice2.region, matchingResult2)
    }
  }

  private def mockSlice(image: BufferedImage, maybeRegion: Option[Region]): Slice = {
    val region = maybeRegion.getOrElse(Region(Anchor.PointOfOrigin, Dimension(image.getWidth, image.getHeight)))
    val slice = mock[Slice]
    when(slice.image).thenReturn(image)
    when(slice.region).thenReturn(region)
    slice
  }

  private def mockImage(width: Int, height: Int): BufferedImage = {
    val reference = mock[BufferedImage]
    when(reference.getWidth).thenReturn(width)
    when(reference.getHeight).thenReturn(height)
    reference
  }
}
