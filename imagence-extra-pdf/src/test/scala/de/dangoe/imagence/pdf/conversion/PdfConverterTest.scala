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

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.pdf.conversion.PdfConverter.{Greyscale, RGB}
import de.dangoe.imagence.testsupport._
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 12.08.16
  */
class PdfConverterTest extends WordSpec with Matchers with ImageFactory with ImageReader {

  "The PdfConverter" should {
    "throw an PdfConversionFailed exception" when {
      "the document cannot be read." in {
        val sut = new PdfConverter
        intercept[PdfConversionFailed] {
          sut.convert(new ByteArrayInputStream(new Array[Byte](0))).head
        }
      }
    }

    "allow to configure the wanted DPI." in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withDpi(150))
      val pageAsImage = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf")).head

      pageAsImage.dimension shouldBe Dimension(1240, 1754)
    }

    "allow to read the document as RGB." in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withImageType(RGB))
      val pageAsImage = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf")).head

      pageAsImage.getType shouldBe BufferedImage.TYPE_INT_RGB
    }

    "allow to read the document as greyscale" in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withImageType(Greyscale))
      val pageAsImage = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf")).head

      pageAsImage.getType shouldBe BufferedImage.TYPE_BYTE_GRAY
    }

    "return a white image" when {
      "an empty document with 300 DPI is converted." in {
        val sut = new PdfConverter
        val convertedDocument = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf"))

        convertedDocument.pageCount shouldBe 1

        convertedDocument.head should showTheSameAs(createImage(Dimension(2479, 3508), Fill(Color.WHITE), BufferedImage.TYPE_INT_RGB))
      }
    }

    "return a sequence of two white images" when {
      "an empty document with two pages is converted." in {
        val sut = new PdfConverter
        val convertedDocument = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_two_pages.pdf"))

        convertedDocument.pageCount shouldBe 2
        convertedDocument.foreach { pageAsImage =>
          pageAsImage should showTheSameAs(createImage(Dimension(2479, 3508), Fill(Color.WHITE), BufferedImage.TYPE_INT_RGB))
        }
      }
    }

    "return a image of the document contents" when {
      "a document with 300 DPI is converted." in {
        val sut = new PdfConverter(PdfConverterConfiguration.default.withDpi(50))
        val convertedDocument = sut.convert(getClass.getResourceAsStream("din_a4_pdf_with_one_page.pdf"))

        convertedDocument.pageCount shouldBe 1

        convertedDocument.head should showTheSameAs(readImage("din_a4_pdf_with_one_page-1.png"))
      }
    }

    "return a sequence of two images of the document contents" when {
      "a document with two pages is converted." in {
        val sut = new PdfConverter(PdfConverterConfiguration.default.withDpi(50))
        val convertedDocument = sut.convert(getClass.getResourceAsStream("din_a4_pdf_with_two_pages.pdf"))

        convertedDocument.pageCount shouldBe 2
        (1 to convertedDocument.pageCount).foreach { pageNumber =>
          convertedDocument.page(pageNumber - 1) should showTheSameAs(readImage(s"din_a4_pdf_with_two_pages-$pageNumber.png"))
        }
      }
    }
  }
}
