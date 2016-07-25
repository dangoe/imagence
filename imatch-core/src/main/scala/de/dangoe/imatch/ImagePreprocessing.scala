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
import java.awt.{Color, Graphics2D}

import de.dangoe.imatch.Colors.{Alpha, Blue, Green, Red, RgbChannel}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 25.07.16
  */
trait ImagePreprocessor extends (BufferedImage => BufferedImage)

trait GreyscaleMethod extends (Int => Int)

abstract class ChannelWeightingGreyscaleMethod extends GreyscaleMethod {
  override def apply(rgb: Int): Int = {
    val greyscaleValue = math.round((for (channel <- Seq(Red, Green, Blue)) yield channel.extract(rgb) * weight(channel)).sum).toInt
    new Color(greyscaleValue, greyscaleValue, greyscaleValue, Alpha.extract(rgb)).getRGB
  }

  protected def weight(channel: RgbChannel): Double
}

case object Averaging extends ChannelWeightingGreyscaleMethod {
  override protected def weight(channel: RgbChannel): Double = 1d / 3d
}

case object Desaturation extends GreyscaleMethod {
  override def apply(rgb: Int): Int = {
    val red = Red.extract(rgb)
    val green = Green.extract(rgb)
    val blue = Blue.extract(rgb)
    val greyscaleValue = math.round((Seq(red, green, blue).max + Seq(red, green, blue).min) / 2d).toInt
    new Color(greyscaleValue, greyscaleValue, greyscaleValue, Alpha.extract(rgb)).getRGB
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
    val g2d = processed.getGraphics.asInstanceOf[Graphics2D]
    Await.ready(Future.sequence {
      for (y <- 0 until image.getHeight) yield processLine(image, g2d, y)
    }, Inf)
    processed
  }

  private def processLine(image: BufferedImage, graphics: Graphics2D, y: Int): Future[Unit] = Future {
    for (x <- 0 until image.getWidth) {
      val rgb = greyscaleMethod(image.getRGB(x, y))
      val color = new Color(rgb, Alpha.extract(rgb) < 255)
      graphics.setColor(color)
      graphics.fillRect(x, y, 1, 1)
    }
  }
}
