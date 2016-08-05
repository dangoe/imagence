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
package de.dangoe.imagence.core.io

import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, Color, Graphics2D}
import java.io.OutputStream
import javax.imageio.ImageIO

import de.dangoe.imagence.api.io.{DifferenceImageData, MatchingResultWriter}
import de.dangoe.imagence.api.matching.{MatchingResult, Regional}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 24.07.16
  */
class DifferenceImageWriter(imageFormat: ImageFormat)
  extends MatchingResultWriter[MatchingResult with Regional] {

  def write(input: DifferenceImageData[MatchingResult with Regional], outputStream: OutputStream): Unit = {
    val processingInput = input.processingInput
    val matchingResults = input.matchingResults

    val reference = processingInput.reference
    val resultImage = new BufferedImage(reference.getWidth, reference.getHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d = resultImage.getGraphics.asInstanceOf[Graphics2D]
    g2d.drawImage(processingInput.reference, 0, 0, null)

    matchingResults.foreach { result =>
      val region = result.region
      val anchor = region.anchor
      val dimension = region.dimension

      val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, result.deviation.value.toFloat)
      g2d.setComposite(ac)
      g2d.drawImage(processingInput.image.getSubimage(anchor.x, anchor.y, dimension.width, dimension.height), anchor.x, anchor.y, null)

      g2d.setColor(new Color(255, 0, 0))
      g2d.fillRect(anchor.x, anchor.y, dimension.width, dimension.height)
    }

    ImageIO.write(resultImage, imageFormat.productPrefix, outputStream)
  }
}

trait ImageFormat {
  def productPrefix: String
}
case object `jpg` extends ImageFormat
case object `png` extends ImageFormat
