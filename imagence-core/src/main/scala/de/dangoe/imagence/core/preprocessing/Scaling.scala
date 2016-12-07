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
package de.dangoe.imagence.core.preprocessing

import java.awt.image.BufferedImage

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.api.preprocessing.Conversion
import org.imgscalr.Scalr
import org.imgscalr.Scalr.{Method, Mode}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
sealed trait ScalingQuality
case object VeryHigh extends ScalingQuality
case object High extends ScalingQuality
case object Normal extends ScalingQuality
case object Speed extends ScalingQuality

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

class Scaling private(bounds: Dimension, method: ScalingMethod)
                     (implicit ec: ExecutionContext, scalingQuality: ScalingQuality) extends Conversion[BufferedImage] {

  import Scaling._

  override def apply(image: BufferedImage) = Future {
    val scaledSize = method.scale(Dimension(image.getWidth, image.getHeight), bounds)
    Scalr.resize(image, toScalingMethod(scalingQuality), Mode.FIT_EXACT, scaledSize.width, scaledSize.height)
  }
}

object Scaling {
  def toBoundingBox(bounds: Dimension)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Speed): Scaling =
    Scaling(bounds, ToBoundingBox)
  def apply(bounds: Dimension, method: ScalingMethod)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Speed): Scaling =
    new Scaling(bounds, method)

  private def toScalingMethod(scalingQuality: ScalingQuality): Method = scalingQuality match {
    case VeryHigh => Method.ULTRA_QUALITY
    case High => Method.QUALITY
    case Normal => Method.BALANCED
    case Speed => Method.SPEED
  }
}

class HarmonizeResolutions private(maybeReferenceScaling: Option[Scaling])
                                  (implicit ec: ExecutionContext, scalingQuality: ScalingQuality) extends Conversion[ProcessingInput] {

  override def apply(input: ProcessingInput) = {
    for {
      scaledReference <- scaleReferenceImage(input)
      scaledImage <- Scaling(scaledReference.dimension, Exact).apply(input.image)
    } yield ProcessingInput(scaledImage, scaledReference)
  }

  private def scaleReferenceImage(input: ProcessingInput) = {
    maybeReferenceScaling match {
      case Some(referenceScaling) => referenceScaling.apply(input.reference)
      case None => Future.successful(input.reference)
    }
  }
}

object HarmonizeResolutions {

  def byScalingToReference()(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Speed): HarmonizeResolutions =
    new HarmonizeResolutions(None)
  def using(referenceScaling: Scaling)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Speed): HarmonizeResolutions =
    new HarmonizeResolutions(Some(referenceScaling))
}
