package de.imagence.pdfbox

import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 12.08.16
  */
class PdfConverterTest extends WordSpec with Matchers {

  val sut = new PdfConverter

  "The PdfConverter" should {
    "return a white image" when {
      "an empty pdf with one page is converted." in {
        ???
      }

      "an empty pdf with two pages is converted." in {
        ???
      }
    }

    "return an image with the document contents" when {
      "a non empty pdf with one page is converted." in {
        ???
      }

      "a non empty pdf with two pages is converted." in {
        ???
      }
    }
  }
}
