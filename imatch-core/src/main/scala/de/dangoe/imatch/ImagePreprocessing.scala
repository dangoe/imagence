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
trait ImagePreprocessor {
  def preprocess(image: BufferedImage): BufferedImage
}

abstract class GreyscaleMethod extends (Int => Int) {
  override final def apply(rgb: Int): Int = {
    val greyscaleValue = math.round((for (channel <- Seq(Red, Green, Blue)) yield channel.extract(rgb) * weight(channel)).sum).toInt
    new Color(greyscaleValue, greyscaleValue, greyscaleValue, Alpha.extract(rgb)).getRGB
  }
  protected def weight(channel: RgbChannel): Double
}

case object Averaging extends GreyscaleMethod {
  override protected def weight(channel: RgbChannel): Double = 1d / 3d
}

class ConvertToGreyscale(greyscaleMethod: GreyscaleMethod) extends ImagePreprocessor {

  implicit val executionContext = ExecutionContext.global

  override def preprocess(image: BufferedImage): BufferedImage = {
    val converted = new BufferedImage(image.getWidth, image.getHeight, image.getType)
    val g2d = converted.getGraphics.asInstanceOf[Graphics2D]
    val op = Future.sequence {
      for (x <- 0 until image.getWidth;
           y <- 0 until image.getHeight) yield Future {
        val rgb = greyscaleMethod(image.getRGB(x, y))
        val color = new Color(rgb, Alpha.extract(rgb) < 255)
        g2d.setColor(color)
        g2d.fillRect(x, y, 1, 1)
      }
    }
    Await.ready(op, Inf)
    converted
  }
}
