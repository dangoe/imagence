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

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 19.07.16
  */
package object matching {

  object ImplicitConversions {
    implicit class RichBufferedImage(delegate: BufferedImage) {
      def aspectRatio: Double = delegate.getWidth.toDouble / delegate.getHeight.toDouble
      def dimension: Dimension = Dimension(delegate.getWidth, delegate.getHeight)
      def isOfSameSizeAs(other: BufferedImage): Boolean = dimension == other.dimension
    }
  }

  case class Anchor(x: Int, y: Int) {
    require(x >= 0, "Horizontal shift must not be smaller than zero.")
    require(y >= 0, "Vertical shift must not be smaller than zero.")
  }

  object Anchor {
    val PointOfOrigin = Anchor(0, 0)
  }

  case class Dimension(width: Int, height: Int) {
    require(width > 0, "Width must be greater than zero.")
    require(height > 0, "Height must be greater than zero.")
    def aspectRatio: Double = width.toDouble / height.toDouble
  }

  case class Region(anchor: Anchor, dimension: Dimension)
}
