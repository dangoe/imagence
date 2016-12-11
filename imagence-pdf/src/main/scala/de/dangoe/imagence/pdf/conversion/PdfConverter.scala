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
package de.dangoe.imagence.pdf.conversion

import java.awt.image.BufferedImage
import java.io.InputStream

import de.dangoe.imagence.pdf.conversion.PdfConverter.{ImageType, RGB}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.{PDFRenderer, ImageType => PdfBoxImageType}

import scala.util.control.NonFatal

class PdfConverter(config: PdfConverterConfiguration = PdfConverterConfiguration.default) {

  import PdfConverter._

  def convert(inputStream: InputStream): ConvertedDocument = try {
    process(PDDocument.load(inputStream))(convert)
  } catch {
    case NonFatal(e) => throw PdfConversionFailed("Failed to convert document.", e)
  }

  private def convert(document: PDDocument): ConvertedDocument = {
    val renderer = new PDFRenderer(document)
    new ConvertedDocument((0 until document.getNumberOfPages).map {
      pageIndex => renderer.renderImageWithDPI(pageIndex, config.dpi, mapImageType(config.imageType))
    })
  }
}

object PdfConverter {

  private def mapImageType(imageType: ImageType): PdfBoxImageType = imageType match {
    case RGB => PdfBoxImageType.RGB
    case Greyscale => PdfBoxImageType.GRAY
  }

  private def process[T](document: PDDocument)(op: PDDocument => T): T =
    try op(document) finally document.close()

  sealed trait ImageType
  object RGB extends ImageType
  object Greyscale extends ImageType
}

class PdfConverterConfiguration private(val dpi: Int, val imageType: ImageType) {
  def withDpi(dpi: Int) = new PdfConverterConfiguration(dpi, imageType)
  def withImageType(imageType: ImageType) = new PdfConverterConfiguration(dpi, imageType)
}

object PdfConverterConfiguration {

  private final val DefaultDpi = 300
  private final val DefaultImageType = RGB

  def default: PdfConverterConfiguration = new PdfConverterConfiguration(DefaultDpi, DefaultImageType)
}

case class PdfConversionFailed(message: String, cause: Throwable) extends RuntimeException(message, cause)

class ConvertedDocument private[pdf](pages: Seq[BufferedImage]) extends Traversable[BufferedImage] {
  override def foreach[U](f: (BufferedImage) => U): Unit = pages.foreach(f)
  def pageCount: Int = pages.length
  def page(index: Int): BufferedImage = pages(index)
}
