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
import scala.math.{ceil, floor, min}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 15.07.16
  */
trait SlicingStrategy {
  def slice(image: BufferedImage): Stream[BufferedImage]
}

class PercentageSlicing(factor: Double) extends SlicingStrategy {

  import PercentageSlicing._

  override def slice(image: BufferedImage): Stream[BufferedImage] =
    slice(image, calculateEdgeLengths(image), 0, 0, Seq.empty).toStream

  @tailrec private final def slice(image: BufferedImage,
                                   sliceEdgeLengths: (Int, Int),
                                   offsetX: Int,
                                   offsetY: Int,
                                   slices: Seq[BufferedImage]): Seq[BufferedImage] = {
    if (offsetY >= image.getHeight) {
      return slices
    }
    slice(
      image,
      sliceEdgeLengths,
      if (offsetX + sliceEdgeLengths._1 < image.getWidth) offsetX + sliceEdgeLengths._1 else 0,
      if (offsetX + sliceEdgeLengths._1 >= image.getWidth) offsetY + sliceEdgeLengths._2 else offsetY,
      slices :+ image.getSubimage(
        offsetX,
        offsetY,
        min(sliceEdgeLengths._1, image.getWidth - offsetX),
        min(sliceEdgeLengths._2, image.getHeight - offsetY)
      )
    )
  }

  private def calculateEdgeLengths(image: BufferedImage): (Int, Int) =
    normalize((ceil(factor * image.getWidth), ceil(factor * image.getHeight)), image.aspectRatio)

  private def normalize(sliceEdgeLengths: (Int, Int), aspectRatio: Double): (Int, Int) = sliceEdgeLengths match {
    case (width, height) if width < MinEdgeLength => (MinEdgeLength, floor(MinEdgeLength * aspectRatio))
    case (width, height) if height < MinEdgeLength => (floor(MinEdgeLength * 1 / aspectRatio), MinEdgeLength)
    case _ => sliceEdgeLengths
  }

  private implicit def doubleToInt(value: Double): Int = value.toInt
}

object PercentageSlicing {
  val MinEdgeLength = 4
}

class Sliceable(image: BufferedImage) {
  def slice(strategy: SlicingStrategy): Stream[BufferedImage] = strategy.slice(image)
}

object ImageSlicer {
  implicit def toSliceable(image: BufferedImage): Sliceable = new Sliceable(image)
}
