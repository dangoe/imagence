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

import com.jhlabs.image.GaussianFilter
import de.dangoe.imagence.api.preprocessing.Conversion

import scala.concurrent.{ExecutionContext, Future}

class GaussianBlur private(radius: Int)(implicit ec: ExecutionContext) extends Conversion[BufferedImage] {

  private val filter = new GaussianFilter(radius)

  override def apply(image: BufferedImage) = Future {
    val destImage = filter.createCompatibleDestImage(image, image.getColorModel)
    filter.filter(image, destImage)
  }
}

object GaussianBlur {
  def apply(radius: Int)(implicit ec: ExecutionContext): GaussianBlur = new GaussianBlur(radius)
}
