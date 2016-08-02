package de.dangoe.imagence.matching

import java.awt.image.BufferedImage

import de.dangoe.imagence.ProcessingInput
import de.dangoe.imagence.matching.Deviation.{MaximumDeviation, NoDeviation}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

/**
  * @author Daniel Götten <daniel.goetten@fashionid.de>
  * @since 02.08.16
  */
class RegionalImageMatcherResultTest extends WordSpec with Matchers with MockitoSugar {

  case class TestMatchingResult(deviationValue: Double) extends MatchingResult {
    override def deviation: Deviation = Deviation(deviationValue)
  }

  "RegionalImageMatcherResult" should {
    "calculate a deviation of zero" when {
      "no RegionalMatchingResults exist." in {
        val processingInput = ProcessingInput(mockBufferedImage(10, 10), mockBufferedImage(10, 10))
        val regionalMatchingResults = Nil

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe NoDeviation
      }

      "no RegionalMatchingResults with a deviation larger than zero exist." in {
        val processingInput = ProcessingInput(mockBufferedImage(10, 10), mockBufferedImage(10, 10))
        val regionalMatchingResults = Seq(RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(10, 10)), TestMatchingResult(0)))

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe NoDeviation
      }
    }

    "calculate a deviation of one" when {
      "RegionalMatchingResults with a deviation of one exist for the whole image." in {
        val processingInput = ProcessingInput(mockBufferedImage(10, 10), mockBufferedImage(10, 10))
        val regionalMatchingResults = Seq(RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(10, 10)), TestMatchingResult(1)))

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe MaximumDeviation
      }
    }


    "calculate a deviation of one half" when {
      "RegionalMatchingResults with a deviation of one exist for one half of the image." in {
        val processingInput = ProcessingInput(mockBufferedImage(10, 10), mockBufferedImage(10, 10))
        val regionalMatchingResults = Seq(
          RegionalMatchingResult(Region(Anchor.PointOfOrigin, Dimension(5, 5)), TestMatchingResult(1)),
          RegionalMatchingResult(Region(Anchor(5, 5), Dimension(5, 5)), TestMatchingResult(1))
        )

        val sut = RegionalImageMatcherResult(processingInput, regionalMatchingResults)

        sut.deviation shouldBe Deviation(0.5)
      }
    }
  }

  private def mockBufferedImage(width: Int, height: Int): BufferedImage = {
    val reference = mock[BufferedImage]
    when(reference.getWidth).thenReturn(width)
    when(reference.getHeight).thenReturn(height)
    reference
  }
}
