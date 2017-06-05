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

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.api.preprocessing.Conversion
import org.imgscalr.Scalr
import org.imgscalr.Scalr.{Method, Mode}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ScalingQuality

object ScalingQuality {
  case object VeryHigh extends ScalingQuality
  case object High extends ScalingQuality
  case object Normal extends ScalingQuality
  case object Low extends ScalingQuality
}

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

  override def apply(image: BufferedImage): Future[BufferedImage] = Future {
    val scaledSize = method.scale(Dimension(image.getWidth, image.getHeight), bounds)
    Scalr.resize(image, toScalingMethod(scalingQuality), Mode.FIT_EXACT, scaledSize.width, scaledSize.height)
  }
}

object Scaling {

  import ScalingQuality._

  def toBoundingBox(bounds: Dimension)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Low): Scaling =
    Scaling(bounds, ToBoundingBox)
  def apply(bounds: Dimension, method: ScalingMethod)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Low): Scaling =
    new Scaling(bounds, method)

  private def toScalingMethod(scalingQuality: ScalingQuality): Method = {
    import ScalingQuality._
    scalingQuality match {
      case VeryHigh => Method.ULTRA_QUALITY
      case High => Method.QUALITY
      case Normal => Method.BALANCED
      case Low => Method.SPEED
    }
  }
}

class HarmonizeResolutions private(maybeReferenceScaling: Option[Scaling])
                                  (implicit ec: ExecutionContext, scalingQuality: ScalingQuality) extends Conversion[ProcessingInput] {

  override def apply(input: ProcessingInput): Future[ProcessingInput] = {

    import de.dangoe.imagence.api.Implicits._

    for {
      scaledReference <- scaleReferenceImage(input)
      scaling = Scaling(scaledReference.dimension, Exact)
      scaledImage <- scaling(input.image)
    } yield ProcessingInput(scaledImage, scaledReference)
  }

  private def scaleReferenceImage(input: ProcessingInput) = {
    maybeReferenceScaling match {
      case Some(referenceScaling) => referenceScaling(input.reference)
      case None => Future.successful(input.reference)
    }
  }
}

object HarmonizeResolutions {

  import ScalingQuality._

  def byScalingToReference(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Low): HarmonizeResolutions =
    new HarmonizeResolutions(None)
  def apply(referenceScaling: Scaling)(implicit ec: ExecutionContext, scalingQuality: ScalingQuality = Low): HarmonizeResolutions =
    new HarmonizeResolutions(Some(referenceScaling))
}
