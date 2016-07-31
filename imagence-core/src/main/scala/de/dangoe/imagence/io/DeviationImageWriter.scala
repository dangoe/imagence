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
package de.dangoe.imagence.io

import java.awt.image.{BufferedImage, ImageObserver}
import java.awt.{AlphaComposite, Color, Graphics2D, Image}
import java.io.OutputStream
import javax.imageio.ImageIO

import de.dangoe.imagence.common.{ImageProcessingContext, ProcessingInput, Prototype}
import de.dangoe.imagence.matching.MatchingResult

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
@Prototype
class DeviationImageWriter() extends ImageObserver {

  def writeTo(processingInput: ProcessingInput, results: Seq[MatchingResult], outputStream: OutputStream): Unit = {
    val resultImage = new BufferedImage(processingInput.reference.getWidth, processingInput.reference.getHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d = resultImage.getGraphics.asInstanceOf[Graphics2D]
    g2d.drawImage(processingInput.reference, 0, 0, this)

    results.foreach { r =>
      val region = r.region
      val anchor = region.anchor
      val dimension = region.dimension

      val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, r.deviation.value.toFloat)
      g2d.setComposite(ac)
      g2d.drawImage(processingInput.image.getSubimage(anchor.x, anchor.y, dimension.width, dimension.height), anchor.x, anchor.y, this)

      g2d.setColor(new Color(255, 0, 0))
      g2d.fillRect(anchor.x, anchor.y, dimension.width, dimension.height)
    }

    ImageIO.write(resultImage, "png", outputStream)
  }

  override def imageUpdate(img: Image, infoflags: Int, x: Int, y: Int, width: Int, height: Int): Boolean = true
}
