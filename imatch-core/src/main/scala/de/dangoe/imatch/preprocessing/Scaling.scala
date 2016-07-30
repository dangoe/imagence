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

import java.awt.RenderingHints
import java.awt.image.BufferedImage

import de.dangoe.imatch.matching.Dimension

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

class Scaling(bounds: Dimension, method: ScalingMethod) extends (BufferedImage => BufferedImage) {
  override def apply(image: BufferedImage): BufferedImage = {
    val scaledSize = method.scale(Dimension(image.getWidth, image.getHeight), bounds)
    val resized = new BufferedImage(scaledSize.width, scaledSize.height, image.getType)
    val g = resized.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    g.drawImage(image, 0, 0, scaledSize.width, scaledSize.height, null)
    g.dispose()
    resized
  }
}
