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

import scala.annotation.tailrec

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
trait SlicingStrategy {
  def slice(image: BufferedImage): Stream[BufferedImage]
}

class QuadraticSlicing(edgeLengthCalculation: Int => Int) extends SlicingStrategy {

  override def slice(image: BufferedImage): Stream[BufferedImage] = {
    require(image.getWidth == image.getHeight, "Image must be quadratic.")
    val sliceEdgeLength = edgeLengthCalculation.apply(image.getWidth)
    require((image.getWidth / sliceEdgeLength.toDouble) % 1 == 0, s"Image edge length is not dividable by $sliceEdgeLength.")
    slice(image, sliceEdgeLength, 0, 0, image.getWidth / sliceEdgeLength, Seq.empty).toStream
  }

  @tailrec final def slice(image: BufferedImage,
                           sliceEdgeLength: Int,
                           column: Int,
                           row: Int,
                           slicesInOneDimension: Int,
                           slices: Seq[BufferedImage]): Seq[BufferedImage] = {
    if (row == slicesInOneDimension) {
      return slices
    }
    slice(image,
      sliceEdgeLength,
      if (column < slicesInOneDimension - 1) column + 1 else 0,
      if (column == slicesInOneDimension - 1) row + 1 else row,
      slicesInOneDimension,
      slices :+ image.getSubimage(column * sliceEdgeLength, row * sliceEdgeLength, sliceEdgeLength, sliceEdgeLength)
    )
  }
}

class Sliceable(image: BufferedImage) {
  def slice(strategy: SlicingStrategy): Stream[BufferedImage] = strategy.slice(image)
}

object ImageSlicer {
  implicit def toSliceable(image: BufferedImage): Sliceable = new Sliceable(image)
}
