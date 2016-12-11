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
import de.dangoe.imagence.core.matching.{DefaultSlicer, PixelWiseColorDeviationMatching, RegionalImageMatcher}
import de.dangoe.imagence.core.preprocessing.HarmonizeResolutions
import de.dangoe.imagence.testsupport._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 03.08.16
  */
class SimpleDifferenceImageWriterTest extends WordSpec with Matchers with ScalaFutures with ImageReader {

  import scala.concurrent.ExecutionContext.Implicits.global

  val harmonization = HarmonizeResolutions.byScalingToReference()
  val regionalImageMatcher = RegionalImageMatcher(
    DefaultSlicer.withFixedSliceSizeOf(Dimension.square(23)),
    PixelWiseColorDeviationMatching(DefaultDeviationCalculatorFactory)
  )

  "DifferenceImageWriter" should {
    "create an expected difference image" when {
      "a specific erroneous image und reference image is used." in {
        val processingInput = ProcessingInput(readImage("pattern_erroneous.png"), readImage("pattern.png"))

        val sut = new SimpleDifferenceImageWriter(`png`)

        whenReady {
          for {
            normalized <- harmonization(processingInput)
            matchingResult <- regionalImageMatcher(normalized)
            outputStream = new ByteArrayOutputStream()
            _ <- Future {
              sut.write(
                DifferenceImageData(
                  matchingResult.processingInput,
                  matchingResult.regionalMatchingResults
                ),
                outputStream
              )
            }
          } yield ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray))
        } { differenceImage =>
          differenceImage should showTheSameAs(readImage("pattern_diff.png"))
        }
      }
    }
  }
}
