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
package de.dangoe.imagence.pdfbox

import java.awt.image.BufferedImage
import java.io.InputStream

import de.dangoe.imagence.pdfbox.PdfConverter.{ImageType, RGB}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.{PDFRenderer, ImageType => PdfBoxImageType}

import scala.util.control.NonFatal

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 12.08.16
  */
class PdfConverter(config: PdfConverterConfiguration = PdfConverterConfiguration.default) {

  import PdfConverter._

  def convert(inputStream: InputStream): BufferedImage = try {
    consume(PDDocument.load(inputStream)) { document =>
      val renderer = new PDFRenderer(document)
      (0 until document.getNumberOfPages).map {
        pageIndex => renderer.renderImageWithDPI(pageIndex, config.dpi, mapImageType(config.imageType))
      }.head
    }
  } catch {
    case NonFatal(e) => throw PdfConversionFailed("Failed to convert document.", e)
  }
}

object PdfConverter {

  private def mapImageType(imageType: ImageType): PdfBoxImageType = imageType match {
    case RGB => PdfBoxImageType.RGB
    case Greyscale => PdfBoxImageType.GRAY
  }

  private def consume[C <: {def close() : Unit}, T](closeable: C)(f: C => T): T = {
    try {
      f(closeable)
    } finally closeable.close()
  }

  sealed trait ImageType
  object RGB extends ImageType
  object Greyscale extends ImageType
}

class PdfConverterConfiguration private(val dpi: Int, val imageType: ImageType) {
  def withDpi(dpi: Int) = new PdfConverterConfiguration(dpi, imageType)
  def withImageType(imageType: ImageType) = new PdfConverterConfiguration(dpi, imageType)
}

object PdfConverterConfiguration {
  def default: PdfConverterConfiguration = new PdfConverterConfiguration(300, RGB)
}

case class PdfConversionFailed(message: String, cause: Throwable) extends RuntimeException(message, cause)
