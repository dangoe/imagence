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
package de.dangoe.imagence.api.matching

import java.awt.image.BufferedImage

import de.dangoe.imagence.api.matching.Anchor.PointOfOrigin

import scala.concurrent.Future

object Slicing {
  object Implicits {
    implicit def toSlice(image: BufferedImage): Slice = Slice(image, PointOfOrigin)
    implicit def extractBufferedImage(slice: Slice): BufferedImage = slice.image
    implicit def toSliceable(image: BufferedImage): Sliceable = new Sliceable(image)
  }
}

class Slice private(val image: BufferedImage, val region: Region) {

  override def toString = s"Slice($image, $region)"

  def canEqual(other: Any): Boolean = other.isInstanceOf[Slice]

  override def equals(other: Any): Boolean = other match {
    case that: Slice =>
      (that canEqual this) &&
        image == that.image &&
        region == that.region
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(image, region)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Slice {
  def apply(image: BufferedImage, anchor: Anchor): Slice = new Slice(image, Region(anchor, Dimension(image.getWidth, image.getHeight)))
}

class Sliceable(image: BufferedImage) {
  def slice(strategy: Slicer): Seq[Future[Slice]] = strategy.slice(image)
}

trait SliceSize

trait SliceSizeCalculation extends (Dimension => Dimension with SliceSize)

trait Slicer {
  def slice(image: BufferedImage): Seq[Future[Slice]]
}

case class Anchor(x: Int, y: Int) {
  require(x >= 0, "Horizontal shift must not be smaller than zero.")
  require(y >= 0, "Vertical shift must not be smaller than zero.")
}

object Anchor {
  val PointOfOrigin = Anchor(0, 0)
}

case class Dimension(width: Int, height: Int) {
  require(width > 0, "Width must be greater than zero.")
  require(height > 0, "Height must be greater than zero.")
  def aspectRatio: Double = width.toDouble / height.toDouble
}

object Dimension {
  def square(edgeLength: Int) = Dimension(edgeLength, edgeLength)
}

case class Region(anchor: Anchor, dimension: Dimension)
