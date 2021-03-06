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

import java.awt.RenderingHints.Key
import java.awt.image.{BufferedImage, ColorConvertOp}
import java.awt.{Color, Graphics2D, RenderingHints}

import de.dangoe.imagence.api.preprocessing.Conversion

import scala.concurrent.{ExecutionContext, Future}

trait GreyscaleMethod extends (Color => Color) {
  protected def createColor(luminance: Int, alpha: Int): Color = new Color(luminance, luminance, luminance, alpha)
}

object GreyscaleMethod {
  case object Averaging extends GreyscaleMethod {
    override def apply(color: Color): Color =
      createColor(math.round((color.getRed + color.getGreen + color.getBlue) / 3d).toInt, color.getAlpha)
  }

  case object Desaturation extends GreyscaleMethod {
    override def apply(color: Color): Color = {
      val maxChannelValue = color.getRed max color.getGreen max color.getBlue
      val minChannelValue = color.getRed min color.getGreen min color.getBlue
      createColor(math.round((maxChannelValue + minChannelValue) / 2d).toInt, color.getAlpha)
    }
  }

  case object Luma extends GreyscaleMethod {
    override def apply(color: Color): Color =
      createColor(math.round(color.getRed * 0.299 + color.getGreen * 0.587 + color.getBlue * 0.114).toInt, color.getAlpha)
  }
}

class GreyscaleConversion private(method: GreyscaleMethod)(implicit ec: ExecutionContext) extends Conversion[BufferedImage] {

  override def apply(image: BufferedImage): Future[BufferedImage] = {
    val greyscaleImage = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_BYTE_GRAY)
    Future.sequence {
      for (y <- 0 until image.getHeight) yield processLine(image, greyscaleImage.getSubimage(0, y, image.getWidth, 1), y)
    }.transform(_ => greyscaleImage, identity)
  }

  private def processLine(source: BufferedImage, target: BufferedImage, y: Int): Future[Unit] = Future {
    val graphics = target.getGraphics.asInstanceOf[Graphics2D]
    0 until target.getWidth foreach { x =>
      graphics.setColor(method(new Color(source.getRGB(x, y), true)))
      graphics.fillRect(x, 0, 1, 1)
    }
  }
}

object GreyscaleConversion {
  def apply(method: GreyscaleMethod)(implicit ec: ExecutionContext): GreyscaleConversion = new GreyscaleConversion(method)
}

class NativeGreyscaleConversion()(implicit ec: ExecutionContext) extends Conversion[BufferedImage] {

  import collection.JavaConverters._

  final val EmptyRenderingHints = new RenderingHints(Map.empty[Key, AnyRef].asJava)

  override def apply(image: BufferedImage) = Future {
    val greyscale = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_BYTE_GRAY)
    new ColorConvertOp(image.getColorModel.getColorSpace, greyscale.getColorModel.getColorSpace, EmptyRenderingHints).filter(image, greyscale)
    greyscale
  }
}

object NativeGreyscaleConversion {
  def apply()(implicit ec: ExecutionContext): NativeGreyscaleConversion = new NativeGreyscaleConversion
}
