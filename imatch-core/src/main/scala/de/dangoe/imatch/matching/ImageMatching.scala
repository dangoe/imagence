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

import java.awt.Color
import java.awt.image.BufferedImage

import de.dangoe.imatch.common.{ImageProcessingContext, Prototype}
import de.dangoe.imatch.matching.Deviation.NoDeviation
import de.dangoe.imatch.matching.ImplicitConversions._
import de.dangoe.imatch.matching.Sliceable._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.{pow, round, sqrt}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
// TODO Draft to be tested or removed
@Prototype
class SlicingImageMatcher[R <: MatchingResult](slicingStrategy: SlicingStrategy, matchingStrategy: MatchingStrategy[R])
                                              (implicit executionContext: ExecutionContext, timeout: Duration) {

  def apply(image: BufferedImage, reference: BufferedImage): Seq[R] = {
    val slices = Await.result(Future.sequence(image.slice(slicingStrategy)), timeout)
    val referenceSlices = Await.result(Future.sequence(reference.slice(slicingStrategy)), timeout)
    val slicePairs = for (i <- slices.indices) yield (slices(i), referenceSlices(i))

    Await.result(Future.sequence(for (partition <- slicePairs.grouped(Runtime.getRuntime.availableProcessors())) yield Future {
      processPartition(partition)
    }), timeout).flatten.toSeq
  }

  private def processPartition(slicePairs: Seq[(Slice, Slice)]): Seq[R] = {
    for (slicePair <- slicePairs) yield matchingStrategy(slicePair._1, slicePair._2)
  }
}

abstract class MatchingStrategy[R <: MatchingResult] {
  final def apply(slice: Slice, reference: Slice): R = {
    if (!slice.image.isOfSameSizeAs(reference)) {
      throw ImageMatchingException("Image dimension differs from reference image!")
    }
    applyInternal(slice, reference)
  }

  protected def applyInternal(slice: Slice, reference: Slice): R
}

trait MatchingResult {
  def context: ImageProcessingContext
  def deviation: Deviation
  def region: Region
}

case class ImageMatchingException(message: String) extends RuntimeException(message)

class PixelWiseColorDeviationMatching private(context: ImageProcessingContext) extends MatchingStrategy[PixelWiseColorDeviationMatchingResult] {

  import de.dangoe.imatch.matching.PixelWiseColorDeviationMatching._

  override protected def applyInternal(slice: Slice, reference: Slice): PixelWiseColorDeviationMatchingResult = {
    val sliceSize = slice.region.dimension
    (for (x <- 0 until sliceSize.width;
          y <- 0 until sliceSize.height;
          deviation <- calculatePixelDeviation(x, y, slice, reference)) yield deviation) match {
      case deviationsByLine if deviationsByLine.nonEmpty =>
        val deviatingPixelCount = deviationsByLine.length
        PixelWiseColorDeviationMatchingResult(
          context,
          Deviation(deviationsByLine.sum / deviatingPixelCount),
          deviatingPixelCount,
          slice.region
        )
      case _ => PixelWiseColorDeviationMatchingResult.withoutDeviation(context, slice.region)
    }
  }

  private def calculatePixelDeviation(x: Int, y: Int, slice: Slice, reference: Slice): Option[Double] = {
    val rgb = new Color(slice.getRGB(x, y))
    val referenceRgb = new Color(reference.getRGB(x, y))
    val euclideanDistance = sqrt(pow(rgb.getRed - referenceRgb.getRed, 2) + pow(rgb.getGreen - referenceRgb.getGreen, 2) + pow(rgb.getBlue - referenceRgb.getBlue, 2))
    euclideanDistance / MaxEuclideanDistance match {
      case d if d > 0 => Some(d)
      case _ => None
    }
  }
}

object PixelWiseColorDeviationMatching {
  val MaxEuclideanDistance: Double = sqrt(3 * pow(255, 2))
  def apply()(implicit context: ImageProcessingContext): PixelWiseColorDeviationMatching = new PixelWiseColorDeviationMatching(context)
}

case class PixelWiseColorDeviationMatchingResult(context: ImageProcessingContext,
                                                 deviation: Deviation,
                                                 deviantPixelCount: Int,
                                                 region: Region) extends MatchingResult

object PixelWiseColorDeviationMatchingResult {
  def withoutDeviation(context: ImageProcessingContext, region: Region): PixelWiseColorDeviationMatchingResult = PixelWiseColorDeviationMatchingResult(context, NoDeviation, 0, region)
}

case class Deviation(value: Double) {
  require(value >= 0, "Value must not be smaller than zero.")
  require(value <= 1, "Value must not be larger than one.")
}

object Deviation {
  val NoDeviation = Deviation(0)
  val MaximumDeviation = Deviation(1)
}
