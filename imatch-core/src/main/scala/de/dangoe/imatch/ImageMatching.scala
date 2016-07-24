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
package de.dangoe.imatch

import java.awt.image.BufferedImage

import de.dangoe.imatch.PercentageDeviation.NoDeviation

import scala.math.abs

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
abstract class MatchingStrategy[R <: MatchingResult] {
  final def evaluate(slice: Slice, reference: Slice): R = {
    if (!slice.image.isOfSameSizeAs(reference)) {
      throw ImageMatchingException("Image dimension differs from reference image!")
    }
    evaluateInternal(slice, reference)
  }

  protected def evaluateInternal(slice: Slice, reference: Slice): R
}

trait MatchingResult {
  def region: Region
}

case class ImageMatchingException(message: String) extends RuntimeException(message)

object SimpleDifferenceMatching extends MatchingStrategy[SimpleDifferenceMatchingResult] {
  override protected def evaluateInternal(slice: Slice, reference: Slice): SimpleDifferenceMatchingResult = {
    val pixelDeviation = for (x <- 0 until slice.getWidth;
                              y <- 0 until slice.getHeight) yield luminance(x, y, slice) - luminance(x, y, reference)
    val absoluteDeviation = abs(pixelDeviation.sum)
    val maxDeviation = 255d * slice.getWidth * slice.getHeight
    SimpleDifferenceMatchingResult(
      maxDeviation match {
        case max if max > 0 => PercentageDeviation(absoluteDeviation / max)
        case _ => NoDeviation
      },
      pixelDeviation.count(_ > 0),
      slice.region
    )
  }

  private def luminance(x: Int, y: Int, image: BufferedImage): Int = ((image.getRGB(x, y) & 0x00ff0000) >> 16) - 128
}

case class SimpleDifferenceMatchingResult(deviation: PercentageDeviation,
                                          deviantPixelCount: Int,
                                          region: Region) extends MatchingResult

case class PercentageDeviation(value: Double) {
  require(value >= 0, "Value must not be smaller than zero.")
  require(value <= 1, "Value must not be larger than one.")
}

object PercentageDeviation {
  val NoDeviation = PercentageDeviation(0)
  val CompleteDeviation = PercentageDeviation(1)
}
