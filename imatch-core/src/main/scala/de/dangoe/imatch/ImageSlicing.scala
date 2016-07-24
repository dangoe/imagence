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

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.{ceil, floor, min}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
trait SlicingStrategy {
  def slice(image: BufferedImage): Seq[Slice]
}

class PercentageSlicing(factor: Double) extends SlicingStrategy {

  import PercentageSlicing._

  implicit val executionContext = ExecutionContext.global

  override def slice(image: BufferedImage): Seq[Slice] = {
    val sliceDimension = calculateSliceDimension(image)
    val horizontalOffsets = 0 until ceil(image.getWidth.toDouble / sliceDimension.width).toInt map (_ * sliceDimension.width)
    val verticalOffsets = 0 until ceil(image.getHeight.toDouble / sliceDimension.height).toInt map (_ * sliceDimension.height)
    Await.result(
      Future.sequence {
        for (horizontalOffset <- horizontalOffsets;
             verticalOffset <- verticalOffsets) yield Future {
          createSlice(Anchor(horizontalOffset, verticalOffset), sliceDimension, image)
        }
      }, Inf)
  }

  private def createSlice(anchor: Anchor, sliceDimension: Dimension, image: BufferedImage): Slice = {
    val width = min(sliceDimension.width, image.getWidth - anchor.x)
    val height = min(sliceDimension.height, image.getHeight - anchor.y)
    Slice(image.getSubimage(anchor.x, anchor.y, width, height), Anchor(anchor.x, anchor.y))
  }

  private def calculateSliceDimension(dimension: Dimension): Dimension =
    normalize(Dimension(ceil(factor * dimension.width), ceil(factor * dimension.height)))

  private def normalize(dimension: Dimension): Dimension = dimension match {
    case d if d.width < MinEdgeLength => Dimension(MinEdgeLength, floor(MinEdgeLength * dimension.aspectRatio))
    case d if d.height < MinEdgeLength => Dimension(floor(MinEdgeLength * 1 / dimension.aspectRatio), MinEdgeLength)
    case _ => dimension
  }

  private implicit def doubleToInt(value: Double): Int = value.toInt
}

object PercentageSlicing {
  val MinEdgeLength = 4
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
