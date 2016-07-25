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

import de.dangoe.imatch.ImplicitConversions._
import de.dangoe.imatch.PercentageSlicing._

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.{ceil, min}
/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
trait SliceSize

abstract class SlicingStrategy(minSliceSize: Dimension with SliceSize) {
  def slice(image: BufferedImage): Seq[Slice]
}

class PercentageSlicing(factor: Double, minSliceSize: Dimension with SliceSize = MinSliceSize)(implicit executionContext: ExecutionContext) extends SlicingStrategy(minSliceSize) {

  override def slice(image: BufferedImage): Seq[Slice] = {
    implicit val sliceSize = calculateSliceSize(image.dimension)
    Await.result(slice(image), Inf)
  }

  def slice(image: BufferedImage)(implicit sliceSize: Dimension with SliceSize) = Future.sequence {
    for (horizontalOffset <- calculateOffsets(image.dimension, _.width);
         verticalOffset <- calculateOffsets(image.dimension, _.height))
      yield createSlice(Anchor(horizontalOffset, verticalOffset), image)
  }

  private def calculateOffsets(dimension: Dimension, edgeLength: Dimension => Int)(implicit sliceSize: Dimension with SliceSize) =
    0 until ceil(edgeLength(dimension).toDouble / edgeLength(sliceSize)).toInt map (_ * edgeLength(sliceSize))

  private def createSlice(anchor: Anchor, image: BufferedImage)(implicit sliceSize: Dimension with SliceSize): Future[Slice] = Future {
    val width = min(sliceSize.width, image.getWidth - anchor.x)
    val height = min(sliceSize.height, image.getHeight - anchor.y)
    Slice(image.getSubimage(anchor.x, anchor.y, width, height), Anchor(anchor.x, anchor.y))
  }

  private def calculateSliceSize(dimension: Dimension): Dimension with SliceSize = {
    Dimension(ceil(factor * dimension.width).toInt, ceil(factor * dimension.height).toInt) match {
      case d if d.width < minSliceSize.width || d.height < minSliceSize.height => minSliceSize
      case d => new Dimension(d.width, d.height) with SliceSize
    }
  }
}

object PercentageSlicing {
  val MinSliceSize = new Dimension(4, 4) with SliceSize
}

class Slice private(val image: BufferedImage, val region: Region)

object Slice {
  def apply(image: BufferedImage, anchor: Anchor): Slice = new Slice(image, Region(anchor, Dimension(image.getWidth, image.getHeight)))

  implicit def toSlice(image: BufferedImage): Slice = Slice(image, Anchor.PointOfOrigin)

  implicit def extractBufferedImage(slice: Slice): BufferedImage = slice.image
}

class Sliceable(image: BufferedImage) {
  def slice(strategy: SlicingStrategy): Seq[Slice] = strategy.slice(image)
}

object Sliceable {
  implicit def toSliceable(image: BufferedImage): Sliceable = new Sliceable(image)
}
