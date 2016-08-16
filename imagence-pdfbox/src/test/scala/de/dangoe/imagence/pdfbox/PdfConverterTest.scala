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

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.matching.Dimension
import de.dangoe.imagence.pdfbox.PdfConverter.{Greyscale, RGB}
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 12.08.16
  */
class PdfConverterTest extends WordSpec with Matchers {

  "The PdfConverter" should {
    "throw an PdfConversionFailed exception" when {
      "the document cannot be read." in {
        val sut = new PdfConverter
        intercept[PdfConversionFailed] {
          sut.convert(new ByteArrayInputStream(new Array[Byte](0)))
        }
      }
    }

    "return a white image" when {
      "an empty document with 300 DPI is converted." in {
        val sut = new PdfConverter
        val image = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf"))

        image.getType shouldBe BufferedImage.TYPE_INT_RGB
        image.dimension shouldBe Dimension(2479, 3508)
        shouldBeCompletelyWhite(image)
      }
    }

    "allow to configure the wanted DPI." in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withDpi(150))
      val image = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf"))

      image.dimension shouldBe Dimension(1240, 1754)
    }

    "allow to read the document as RGB." in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withImageType(RGB))
      val image = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf"))

      image.getType shouldBe BufferedImage.TYPE_INT_RGB
    }

    "allow to read the document as greyscale" in {
      val sut = new PdfConverter(PdfConverterConfiguration.default.withImageType(Greyscale))
      val image = sut.convert(getClass.getResourceAsStream("empty_din_a4_pdf_with_one_page.pdf"))

      image.getType shouldBe BufferedImage.TYPE_BYTE_GRAY
    }
  }

  // TODO Add testhelper module that can be used by all project modules
  private def shouldBeCompletelyWhite(image: BufferedImage): Unit = for (x <- 0 until image.getWidth;
                                                                         y <- 0 until image.getHeight) {
    new Color(image.getRGB(x, y)) shouldBe Color.WHITE
  }
}
