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

import scala.math.abs

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
abstract class MatchingStrategy[R <: MatchingResult] {
  final def evaluate(image: BufferedImage, reference: BufferedImage): R = {
    if ((image.getWidth, image.getHeight) != (reference.getWidth, reference.getHeight)) {
      throw MatchingNotPossible("Image and reference image are not of same size!")
    }
    evaluateInternal(image, reference)
  }

  protected def evaluateInternal(image: BufferedImage, reference: BufferedImage): R
}

trait MatchingResult

object SimpleDifferenceMatching extends MatchingStrategy[SimpleDifferenceMatchingResult] {
  override protected def evaluateInternal(image: BufferedImage, reference: BufferedImage): SimpleDifferenceMatchingResult = {
    val pixelDeviation = for (x <- 0 until image.getWidth;
                              y <- 0 until image.getHeight) yield luminance(x, y, image) - luminance(x, y, reference)
    val absoluteDeviation = abs(pixelDeviation.sum)
    val maxDeviation = 255d * image.getWidth * image.getHeight
    SimpleDifferenceMatchingResult(if (absoluteDeviation > 0) absoluteDeviation / maxDeviation else 0, pixelDeviation.count(_ > 0))
  }

  private def luminance(x: Int, y: Int, image: BufferedImage): Int = ((image.getRGB(x, y) & 0x00ff0000) >> 16) - 128
}

case class SimpleDifferenceMatchingResult(deviation: Double, deviantPixelCount: Int) extends MatchingResult

case class MatchingNotPossible(message: String) extends RuntimeException(message)
