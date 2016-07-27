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
package de.dangoe.imatch.matching

import de.dangoe.imatch.common.Colors._
import de.dangoe.imatch.common.ImageProcessingContext
import de.dangoe.imatch.matching.Deviation._
import de.dangoe.imatch.matching.ImplicitConversions._

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

  protected def evaluateInternal(slice: Slice, reference: Slice)(implicit context: ImageProcessingContext): R
}

trait MatchingResult {
  def context: ImageProcessingContext
  def deviation: Deviation
  def region: Region
}

case class ImageMatchingException(message: String) extends RuntimeException(message)

object PixelWiseColorDeviationMatching extends MatchingStrategy[PixelWiseColorDeviationMatchingResult] {
  override protected def evaluateInternal(slice: Slice, reference: Slice)(implicit context: ImageProcessingContext): PixelWiseColorDeviationMatchingResult = {
    val deviationByPixel = for (x <- 0 until slice.getWidth;
                                y <- 0 until slice.getHeight;
                                deviationOfPixel <- calculateDeviation(x, y, slice, reference).map(d => (x, y, d))) yield deviationOfPixel
    val deviation = deviationByPixel.nonEmpty match {
      case true =>
        val maxDeviation = (255d * 3) * slice.getWidth * slice.getHeight
        Deviation(deviationByPixel.map(_._3).sum / maxDeviation)
      case false => NoDeviation
    }
    val deviantPixelCount = deviationByPixel.count(_._3 > 0d)
    PixelWiseColorDeviationMatchingResult(context, deviation, deviantPixelCount, slice.region)
  }

  private def calculateDeviation(x: Int, y: Int, slice: Slice, reference: Slice): Option[Int] = {
    val rgb = slice.getRGB(x, y)
    val referenceRgb = reference.getRGB(x, y)
    (for (channel <- Seq(Red, Green, Blue)) yield abs(channel.extract(rgb) - channel.extract(referenceRgb))).sum match {
      case d if d > 0 => Some(d)
      case _ => None
    }
  }
}

case class PixelWiseColorDeviationMatchingResult(context: ImageProcessingContext,
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
