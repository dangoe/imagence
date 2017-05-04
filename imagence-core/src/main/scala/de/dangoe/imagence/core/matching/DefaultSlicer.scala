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
import java.time.LocalDateTime

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching._

import scala.concurrent.{ExecutionContext, Future}
import scala.math.{ceil, min}

class DefaultSlicer private(sliceSizeCalculation: SliceSizeCalculation)(implicit ec: ExecutionContext) extends Slicer {

  override def slice(image: BufferedImage): Seq[Future[Slice]] = {
    implicit val sliceSize = sliceSizeCalculation(image.dimension)
    for {
      verticalOffset <- calculateOffsets(image.dimension, _.height)
      horizontalOffset <- calculateOffsets(image.dimension, _.width)
    } yield Future {
      val width = min(sliceSize.width, image.getWidth - horizontalOffset)
      val height = min(sliceSize.height, image.getHeight - verticalOffset)
      Slice(image.getSubimage(horizontalOffset, verticalOffset, width, height), Anchor(horizontalOffset, verticalOffset))
    }
  }

  private def calculateOffsets(dimension: Dimension, edgeLength: Dimension => Int)(implicit sliceSize: Dimension with SliceSize) =
    0 until ceil(edgeLength(dimension).toDouble / edgeLength(sliceSize)).toInt map (_ * edgeLength(sliceSize))
}

object DefaultSlicer {
  def apply(sliceSizeCalculation: SliceSizeCalculation)(implicit ec: ExecutionContext): DefaultSlicer =
    new DefaultSlicer(sliceSizeCalculation)

  def withFixedSliceSizeOf(dimension: Dimension)(implicit ec: ExecutionContext): DefaultSlicer =
    new DefaultSlicer(FixedSliceSize(dimension))
  def withPercentageSliceSizeOf(factor: Double)(implicit ec: ExecutionContext): DefaultSlicer =
    new DefaultSlicer(PercentageSliceSize(factor))
}

case class FixedSliceSize(value: Dimension) extends SliceSizeCalculation {

  override def apply(dimension: Dimension): Dimension with SliceSize = new Dimension(value.width, value.height) with SliceSize
}

case class RelativeSliceSize(factor: Int) extends SliceSizeCalculation {

  require(factor > 0, "Factor must be larger than zero.")

  override def apply(dimension: Dimension): Dimension with SliceSize = {
    val reciprocal = 1d / factor
    new Dimension(ceil(reciprocal * dimension.width).toInt, ceil(reciprocal * dimension.height).toInt) with SliceSize
  }
}

object RelativeSliceSize {
  final val OneHalf = 2
  final val OneQuater = 4
  final val OneEighth = 8
  final val OneSixteenth = 16
}

@deprecated(message = s"Use ${classOf[RelativeSliceSize].getSimpleName} instead.", since = LocalDateTime.now().toString)
case class PercentageSliceSize(factor: Double) extends SliceSizeCalculation {

  //noinspection ScalaDeprecation
  import PercentageSliceSize._

  require(factor > 0, "Factor must be larger than zero.")
  require(factor <= 1, "Factor must not be larger than one.")

  override def apply(dimension: Dimension): Dimension with SliceSize = {
    Dimension(ceil(factor * dimension.width).toInt, ceil(factor * dimension.height).toInt) match {
      case d if d.width < MinSliceSize.width || d.height < MinSliceSize.height => MinSliceSize
      case d => new Dimension(d.width, d.height) with SliceSize
    }
  }
}

@deprecated(message = s"Use ${classOf[RelativeSliceSize].getSimpleName} instead.", since = LocalDateTime.now().toString)
object PercentageSliceSize {
  val MinSliceSize = new Dimension(1, 1) with SliceSize
}
