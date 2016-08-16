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

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.{ceil, min}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
class DefaultSlicer private(sliceSizeCalculation: SliceSizeCalculation)
                           (implicit executionContext: ExecutionContext, timeout:Duration) extends Slicer {

  override def slice(image: BufferedImage): Seq[Slice] = {
    implicit val sliceSize = sliceSizeCalculation(image.dimension)
    Await.result(Future.sequence(
      for (verticalOffset <- calculateOffsets(image.dimension, _.height)) yield createSlice(verticalOffset, image)
    ), timeout).flatten
  }

  private def calculateOffsets(dimension: Dimension, edgeLength: Dimension => Int)(implicit sliceSize: Dimension with SliceSize) =
    0 until ceil(edgeLength(dimension).toDouble / edgeLength(sliceSize)).toInt map (_ * edgeLength(sliceSize))

  private def createSlice(verticalOffset: Int, image: BufferedImage)(implicit sliceSize: Dimension with SliceSize): Future[Seq[Slice]] = Future {
    for (horizontalOffset <- calculateOffsets(image.dimension, _.width)) yield {
      val width = min(sliceSize.width, image.getWidth - horizontalOffset)
      val height = min(sliceSize.height, image.getHeight - verticalOffset)
      Slice(image.getSubimage(horizontalOffset, verticalOffset, width, height), Anchor(horizontalOffset, verticalOffset))
    }
  }
}

object DefaultSlicer {
  def apply(sliceSizeCalculation: SliceSizeCalculation)(implicit executionContext: ExecutionContext, timeout: Duration): DefaultSlicer =
    new DefaultSlicer(sliceSizeCalculation)

  def withFixedSliceSizeOf(dimension: Dimension)(implicit executionContext: ExecutionContext, timeout: Duration): DefaultSlicer =
    new DefaultSlicer(FixedSliceSize(dimension))
  def withPercentageSliceSizeOf(factor: Double)(implicit executionContext: ExecutionContext, timeout: Duration): DefaultSlicer =
    new DefaultSlicer(PercentageSliceSize(factor))
}

case class FixedSliceSize(value: Dimension) extends SliceSizeCalculation {

  override def apply(dimension: Dimension): Dimension with SliceSize = new Dimension(value.width, value.height) with SliceSize
}

case class PercentageSliceSize(factor: Double) extends SliceSizeCalculation {

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

object PercentageSliceSize {
  val MinSliceSize = new Dimension(1, 1) with SliceSize
}