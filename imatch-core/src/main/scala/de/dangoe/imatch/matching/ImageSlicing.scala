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

import java.awt.image.BufferedImage

import de.dangoe.imatch.matching.ImplicitConversions._

import scala.concurrent.{ExecutionContext, Future}
import scala.math.{ceil, min}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
trait SliceSize

trait SliceSizeCalculation extends (Dimension => Dimension with SliceSize)

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

trait SlicingStrategy {
  def slice(image: BufferedImage): Seq[Future[Slice]]
}

class DefaultSlicing private (sliceSizeCalculation: SliceSizeCalculation)(implicit executionContext: ExecutionContext) extends SlicingStrategy {

  override def slice(image: BufferedImage): Seq[Future[Slice]] = {
    val imageSize = image.dimension
    implicit val sliceSize = sliceSizeCalculation(imageSize)
    for (horizontalOffset <- calculateOffsets(imageSize, _.width);
         verticalOffset <- calculateOffsets(imageSize, _.height)) yield createSlice(Anchor(horizontalOffset, verticalOffset), image)
  }

  private def calculateOffsets(dimension: Dimension, edgeLength: Dimension => Int)(implicit sliceSize: Dimension with SliceSize) =
    0 until ceil(edgeLength(dimension).toDouble / edgeLength(sliceSize)).toInt map (_ * edgeLength(sliceSize))

  private def createSlice(anchor: Anchor, image: BufferedImage)(implicit sliceSize: Dimension with SliceSize): Future[Slice] = Future {
    val width = min(sliceSize.width, image.getWidth - anchor.x)
    val height = min(sliceSize.height, image.getHeight - anchor.y)
    Slice(image.getSubimage(anchor.x, anchor.y, width, height), Anchor(anchor.x, anchor.y))
  }
}

object DefaultSlicing {
  def apply(sliceSizeCalculation: SliceSizeCalculation)(implicit executionContext: ExecutionContext): DefaultSlicing =
    new DefaultSlicing(sliceSizeCalculation)
}

class Slice private(val image: BufferedImage, val region: Region)

object Slice {
  def apply(image: BufferedImage, anchor: Anchor): Slice = new Slice(image, Region(anchor, Dimension(image.getWidth, image.getHeight)))

  implicit def toSlice(image: BufferedImage): Slice = Slice(image, Anchor.PointOfOrigin)
  implicit def extractBufferedImage(slice: Slice): BufferedImage = slice.image
}

class Sliceable(image: BufferedImage) {
  def slice(strategy: SlicingStrategy): Seq[Future[Slice]] = strategy.slice(image)
}

object Sliceable {
  implicit def toSliceable(image: BufferedImage): Sliceable = new Sliceable(image)
}
