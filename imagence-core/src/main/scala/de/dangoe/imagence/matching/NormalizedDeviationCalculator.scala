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

import java.awt.Color
import java.awt.image.BufferedImage

import de.dangoe.imagence.common.ProcessingInput

import scala.math._

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 31.07.16
  */
trait NormalizedDeviationCalculator {
  def calculate(color: Color, referenceColor: Color): Option[Double]
}

class EuclideanDistanceCalculator(input: ProcessingInput) extends NormalizedDeviationCalculator {

  import EuclideanDistanceCalculator._

  private final val greyscale = input.image.getType == BufferedImage.TYPE_BYTE_GRAY && input.image.getType == BufferedImage.TYPE_BYTE_GRAY

  override def calculate(color: Color, referenceColor: Color): Option[Double] = {
    val deviation = if (greyscale) calculateGreyscale(color, referenceColor) else calculateColor(color, referenceColor)
    if (deviation > 0) Some(deviation) else None
  }

  @inline private def calculateColor(color: Color, referenceColor: Color): Double = {
    sqrt(
      pow(color.getRed - referenceColor.getRed, 2)
        + pow(color.getGreen - referenceColor.getGreen, 2)
        + pow(color.getBlue - referenceColor.getBlue, 2)
    ) / MaxEuclideanDistance
  }

  @inline private def calculateGreyscale(color: Color, referenceColor: Color): Double = {
    sqrt(
      pow(color.getRed - referenceColor.getRed, 2)
    ) / MaxEuclideanDistanceGreyscale
  }
}

object EuclideanDistanceCalculator {
  private final val MaxEuclideanDistance: Double = sqrt(3 * pow(255, 2))
  private final val MaxEuclideanDistanceGreyscale: Double = sqrt(pow(255, 2))
}
