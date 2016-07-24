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

import java.awt.Color
import java.awt.image.BufferedImage

import de.dangoe.imatch.Deviation.NoDeviation
import de.dangoe.imatch.Colors._

import scala.math.abs

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
abstract class MatchingStrategy[R <: MatchingResult] {
  final def evaluate(slice: Slice, reference: Slice)(implicit context: ImageProcessingContext): R = {
    if (!slice.image.isOfSameSizeAs(reference)) {
      throw ImageMatchingException("Image dimension differs from reference image!")
    }
    evaluateInternal(slice, reference)
  }

  protected def evaluateInternal(slice: Slice, reference: Slice)(implicit context:ImageProcessingContext): R
}

trait MatchingResult {
  def context: ImageProcessingContext
  def deviation: Deviation
  def region: Region
}

case class ImageMatchingException(message: String) extends RuntimeException(message)

object SimpleDifferenceMatching extends MatchingStrategy[SimpleDifferenceMatchingResult] {
  override protected def evaluateInternal(slice: Slice, reference: Slice)(implicit context: ImageProcessingContext): SimpleDifferenceMatchingResult = {
    val luminanceDeviationByPixel = for (x <- 0 until slice.getWidth;
                                         y <- 0 until slice.getHeight;
                                         deviationOfPixel <- luminanceDeviation(x, y, slice, reference).map(d => (x, y, d))) yield deviationOfPixel
    val deviation = luminanceDeviationByPixel.nonEmpty match {
      case true => {
        val maxDeviation = 255d * slice.getWidth * slice.getHeight
        Deviation(luminanceDeviationByPixel.map(_._3).sum / maxDeviation)
      }
      case false => NoDeviation
    }
    val deviantPixelCount = luminanceDeviationByPixel.count(_._3 > 0d)
    SimpleDifferenceMatchingResult(context, deviation, deviantPixelCount, slice.region)
  }

  private def luminanceDeviation(x: Int, y: Int, slice: Slice, reference: Slice): Option[Int] = {
    val deviation = abs(luminance(x, y, slice) - luminance(x, y, reference))
    if(deviation > 0) Some(deviation) else None
  }

  private def luminance(x: Int, y: Int, image: BufferedImage): Int =
    new Color(image.getRGB(x, y)).greyscale.getRed
}

case class SimpleDifferenceMatchingResult(context: ImageProcessingContext,
                                          deviation: Deviation,
                                          deviantPixelCount: Int,
                                          region: Region) extends MatchingResult

case class Deviation(value: Double) {
  require(value >= 0, "Value must not be smaller than zero.")
  require(value <= 1, "Value must not be larger than one.")
}

object Deviation {
  val NoDeviation = Deviation(0)
  val MaximumDeviation = Deviation(1)
}
