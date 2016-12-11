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
package de.dangoe.imagence.core.matching

import java.awt.image.BufferedImage

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.{Deviation, NormalizedDeviationCalculator}
import de.dangoe.imagence.core.matching.RgbChannels._

import scala.math._

class EuclideanDistanceCalculator(input: ProcessingInput) extends NormalizedDeviationCalculator {

  import EuclideanDistanceCalculator._

  private final val greyscale = input.image.getType == BufferedImage.TYPE_BYTE_GRAY && input.image.getType == BufferedImage.TYPE_BYTE_GRAY

  override def calculate(rgb: Int, referenceRgb: Int): Option[Deviation] = {
    val deviation = if (greyscale) calculateGreyscale(rgb, referenceRgb) else calculateColor(rgb, referenceRgb)
    if (deviation > 0) Some(Deviation(deviation)) else None
  }

  @inline private def calculateColor(rgb: Int, referenceRgb: Int): Double = {
    sqrt(
      pow(extractRed(rgb) - extractRed(referenceRgb), 2)
        + pow(extractGreen(rgb) - extractGreen(referenceRgb), 2)
        + pow(extractBlue(rgb) - extractBlue(referenceRgb), 2)
    ) / MaxEuclideanDistance
  }

  @inline private def calculateGreyscale(rgb: Int, referenceRgb: Int): Double = {
    sqrt(
      pow(extractRed(rgb) - extractRed(referenceRgb), 2)
    ) / MaxEuclideanDistanceGreyscale
  }
}

object EuclideanDistanceCalculator {
  private final val MaxEuclideanDistance: Double = sqrt(3 * pow(255, 2))
  private final val MaxEuclideanDistanceGreyscale: Double = sqrt(pow(255, 2))
}
