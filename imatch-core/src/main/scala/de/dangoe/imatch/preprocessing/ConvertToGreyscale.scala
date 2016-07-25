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

import java.awt.Graphics2D
import java.awt.image.BufferedImage

import de.dangoe.imatch.Colors.ImplicitConversions._
import de.dangoe.imatch.Colors._

import scala.concurrent.duration.Duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 25.07.16
  */
trait GreyscaleMethod extends (Color => Color)

abstract class ChannelWeightingGreyscaleMethod extends GreyscaleMethod {
  override def apply(color: Color): Color = {
    val luminance = math.round(color.red * weight(Red) + color.green * weight(Green) + color.blue * weight(Blue)).toInt
    Color.grey(luminance, color.alpha)
  }
  protected def weight(channel: RgbChannel): Double
}

case object Averaging extends ChannelWeightingGreyscaleMethod {
  override protected def weight(channel: RgbChannel): Double = 1d / 3d
}

case object Desaturation extends GreyscaleMethod {
  override def apply(color: Color): Color = {
    val channelValues = Set(color.red, color.green, color.blue)
    val luminance = math.round((channelValues.max + channelValues.min) / 2d).toInt
    Color.grey(luminance, color.alpha)
  }
}

case object Luma extends ChannelWeightingGreyscaleMethod {
  override protected def weight(channel: RgbChannel): Double = channel match {
    case Red => 0.299
    case Green => 0.587
    case Blue => 0.114
    case _ => 0
  }
}

class ConvertToGreyscale(greyscaleMethod: GreyscaleMethod)(implicit executionContext: ExecutionContext) extends ImagePreprocessor {

  override def apply(image: BufferedImage): BufferedImage = {
    val processed = new BufferedImage(image.getWidth, image.getHeight, image.getType)
    val graphics = processed.getGraphics.asInstanceOf[Graphics2D]
    Await.ready(Future.sequence {
      for (y <- 0 until image.getHeight) yield processLine(image, graphics, y)
    }, Inf)
    processed
  }

  private def processLine(image: BufferedImage, graphics: Graphics2D, y: Int): Future[Unit] = Future {
    0 until image.getWidth foreach { x =>
      val color = greyscaleMethod(Color.fromRGB(image.getRGB(x, y)))
      graphics.setColor(color.asJava)
      graphics.fillRect(x, y, 1, 1)
    }
  }
}
