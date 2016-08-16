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
package de.dangoe.imagence.core.io

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.io.DifferenceImageData
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.core.matching.PixelWiseColorDeviationMatching._
import de.dangoe.imagence.core.matching.{DefaultSlicer, RegionalImageMatcher}
import de.dangoe.imagence.core.preprocessing.HarmonizeResolutions
import de.dangoe.imagence.core.testsupport._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 03.08.16
  */
class DifferenceImageWriterTest extends WordSpec with Matchers with ImageReader {

  implicit val executionContext = ExecutionContext.global
  implicit val timeout = 15 seconds

  "DifferenceImageWriter" should {
    "create an expected difference image" when {
      "a specific erroneous image und reference image is used." in {
        val processingInput = ProcessingInput(readImage("pattern_erroneous.png"), readImage("pattern.png"))

        val result = HarmonizeResolutions.byScalingToReference()
          .andThen(RegionalImageMatcher(
            DefaultSlicer.withFixedSliceSizeOf(Dimension.square(23)),
            DefaultPixelWiseColorDeviationMatching)
          )
          .apply(processingInput)

        val sut = new DifferenceImageWriter(`png`)

        val outputStream = new ByteArrayOutputStream()
        sut.write(
          DifferenceImageData(
            result.processingInput,
            result.regionalMatchingResults
          ),
          outputStream
        )
        val differenceImage = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray))

        differenceImage should showTheSameAs(readImage("pattern_diff.png"))
      }
    }
  }
}