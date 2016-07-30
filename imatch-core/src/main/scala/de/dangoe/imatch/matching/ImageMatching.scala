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

import de.dangoe.imatch.common._
import de.dangoe.imatch.matching.Deviation.NoDeviation
import de.dangoe.imatch.matching.ImplicitConversions._
import de.dangoe.imatch.matching.Sliceable._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.{pow, sqrt}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
// TODO Draft to be tested or removed
@Prototype
class SlicingImageMatching[R <: MatchingResult] private(slicingStrategy: SlicingStrategy, matchingStrategy: MatchingStrategy[R])
                                                       (implicit executionContext: ExecutionContext, timeout: Duration) extends ((ProcessingInput) => (ProcessingInput, Seq[R])) {

  override def apply(processingInput: ProcessingInput): (ProcessingInput, Seq[R]) = {
    require(processingInput.image.dimension == processingInput.reference.dimension, "Image and reference image must be of same size!")

    val slices = Await.result(Future.sequence(processingInput.image.slice(slicingStrategy)), timeout)
    val referenceSlices = Await.result(Future.sequence(processingInput.reference.slice(slicingStrategy)), timeout)
    val slicePairs = for (i <- slices.indices) yield (slices(i), referenceSlices(i))

    (processingInput, Await.result(Future.sequence(for (partition <- slicePairs.grouped(Runtime.getRuntime.availableProcessors())) yield Future {
      processPartition(partition)
    }), timeout).flatten.toSeq)
  }

  private def processPartition(slicePairs: Seq[(Slice, Slice)]): Seq[R] = {
    for (slicePair <- slicePairs) yield matchingStrategy(slicePair._1, slicePair._2)
  }
}

object SlicingImageMatching {
  def apply[R <: MatchingResult](slicingStrategy: SlicingStrategy, matchingStrategy: MatchingStrategy[R])
                                (implicit executionContext: ExecutionContext, timeout: Duration): SlicingImageMatching[R] =
    new SlicingImageMatching[R](slicingStrategy, matchingStrategy)
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

  import PixelWiseColorDeviationMatching._

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

  @inline private def calculatePixelDeviation(x: Int, y: Int, slice: Slice, reference: Slice): Option[Double] = {
    val maxEuclideanDistance = if (context.greyscaleMode) MaxEuclideanDistanceGreyscale else MaxEuclideanDistance
    euclideanDistance(new Color(slice.getRGB(x, y)), new Color(reference.getRGB(x, y))) / maxEuclideanDistance match {
      case d if d > 0 => Some(d)
      case _ => None
    }
  }

  @inline private def euclideanDistance(color: Color, referenceColor: Color): Double = {
    if (context.greyscaleMode) {
      sqrt(pow(color.getRed - referenceColor.getRed, 2))
    } else {
      sqrt(pow(color.getRed - referenceColor.getRed, 2) + pow(color.getGreen - referenceColor.getGreen, 2) + pow(color.getBlue - referenceColor.getBlue, 2))
    }
  }
}

object PixelWiseColorDeviationMatching {
  val MaxEuclideanDistance: Double = sqrt(3 * pow(255, 2))
  val MaxEuclideanDistanceGreyscale: Double = sqrt(pow(255, 2))

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
