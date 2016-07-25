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

import java.awt.{Color => JavaColor}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
object Colors {

  object ImplicitConversions {
    implicit def colorFromRgb(rgb: Int): Color = Color(Red.extract(rgb), Green.extract(rgb), Blue.extract(rgb), Alpha.extract(rgb))
    implicit class ConvertibleToJavaColor(color: Color) {
      def asJava: JavaColor = new JavaColor(color.toRGB, color.withTransparency)
    }
  }

  case class Color(red: Int, green: Int, blue: Int, alpha: Int = 255) {
    Red.check(red)
    Green.check(green)
    Blue.check(blue)
    Alpha.check(alpha)
    private val rgb = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0)
    def toRGB: Int = rgb
    def withTransparency: Boolean = alpha < 255
  }

  object Color {
    def fromRGB(rgb: Int): Color = Color(Red.extract(rgb), Green.extract(rgb), Blue.extract(rgb), Alpha.extract(rgb))
    def grey(luminance: Int, alpha: Int): Color = Color(luminance: Int, luminance: Int, luminance: Int, alpha: Int)
  }

  sealed trait RgbChannel {
    def check(luminance: Int): Unit = {
      require(luminance >= 0, s"$channelName channel value must be greater or equal than zero.")
      require(luminance <= 255, s"$channelName channel value must be smaller or equal than 255.")
    }
    def extract(rgb: Int): Int
    protected def channelName: String
  }
  case object Red extends RgbChannel {
    override def extract(rgb: Int): Int = (rgb >> 16) & 0x000000FF
    override protected def channelName: String = productPrefix
  }
  case object Green extends RgbChannel {
    override def extract(rgb: Int): Int = (rgb >> 8) & 0x000000FF
    override protected def channelName: String = productPrefix
  }
  case object Blue extends RgbChannel {
    override def extract(rgb: Int): Int = rgb & 0x000000FF
    override protected def channelName: String = productPrefix
  }
  case object Alpha extends RgbChannel {
    override def extract(rgb: Int): Int = (rgb >> 24) & 0x000000FF
    override protected def channelName: String = productPrefix
  }
}
