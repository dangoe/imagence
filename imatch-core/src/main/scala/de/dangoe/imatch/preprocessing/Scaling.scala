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
package de.dangoe.imatch.preprocessing

import java.awt.image.BufferedImage

import de.dangoe.imatch.common.{ImageProcessingContext, ProcessingInput}
import de.dangoe.imatch.matching.Dimension
import de.dangoe.imatch.matching.ImplicitConversions.RichBufferedImage
import org.imgscalr.Scalr
import org.imgscalr.Scalr.{Method, Mode}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
sealed trait ScalingMethod {
  private[preprocessing] def scale(dimension: Dimension, bounds: Dimension): Dimension
}

case object ToWidth extends ScalingMethod {
  override private[preprocessing] def scale(dimension: Dimension, bounds: Dimension): Dimension =
    Dimension(bounds.width, (bounds.width * 1d / dimension.aspectRatio).toInt)
}

case object ToHeight extends ScalingMethod {
  override private[preprocessing] def scale(dimension: Dimension, bounds: Dimension): Dimension =
    Dimension((bounds.height * dimension.aspectRatio).toInt, bounds.height)
}

case object ToBoundingBox extends ScalingMethod {
  override private[preprocessing] def scale(dimension: Dimension, bounds: Dimension): Dimension = dimension.aspectRatio match {
    case aspectRatio if aspectRatio > 1 => ToWidth.scale(dimension, bounds)
    case _ => ToHeight.scale(dimension, bounds)
  }
}

case object Exact extends ScalingMethod {
  override private[preprocessing] def scale(dimension: Dimension, bounds: Dimension): Dimension =
    Dimension(bounds.width, bounds.height)
}

class Scaling private(bounds: Dimension, method: ScalingMethod) extends (BufferedImage => BufferedImage) {

  override def apply(image: BufferedImage): BufferedImage = {
    val scaledSize = method.scale(Dimension(image.getWidth, image.getHeight), bounds)
    Scalr.resize(image, Method.QUALITY, Mode.FIT_EXACT, scaledSize.width, scaledSize.height)
  }
}

object Scaling {
  def apply(bounds: Dimension, method: ScalingMethod): Scaling = new Scaling(bounds, method)
}

class HarmonizeResolutions private()(implicit context: ImageProcessingContext, executionContext: ExecutionContext, timeout: Duration)
  extends (ProcessingInput => ProcessingInput) {

  override def apply(input: ProcessingInput): ProcessingInput = {
    ProcessingInput(Await.result(Future(Scaling(input.reference.dimension, Exact).apply(input.image)), timeout), input.reference)
  }
}

object HarmonizeResolutions {

  def apply()(implicit context: ImageProcessingContext, executionContext: ExecutionContext, timeout: Duration): HarmonizeResolutions =
    new HarmonizeResolutions()
}

object Resolution {
  def veryLow: Dimension = Dimension(320, 320)
  def low: Dimension = Dimension(640, 640)
  def medium: Dimension = Dimension(1024, 1024)
  def high: Dimension = Dimension(1440, 1440)
  def veryHigh: Dimension = Dimension(1920, 1920)
}
