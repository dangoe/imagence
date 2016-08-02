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
package de.dangoe.imagence.matching

import java.awt.Color

import de.dangoe.imagence.Implicits._
import de.dangoe.imagence.ProcessingInput
import de.dangoe.imagence.matching.Deviation.NoDeviation

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class PixelWiseColorDeviationMatching private(deviationCalculatorFactory: (ProcessingInput => NormalizedDeviationCalculator))
  extends Matcher[PixelWiseColorDeviationMatchingResult] {

  override protected def applyInternal(input: ProcessingInput): PixelWiseColorDeviationMatchingResult = {
    val deviationCalculator = deviationCalculatorFactory(input)
    val imageSize = input.image.dimension
    (for (x <- 0 until imageSize.width;
          y <- 0 until imageSize.height;
          deviation <- deviationCalculator.calculate(new Color(input.image.getRGB(x, y)), new Color(input.reference.getRGB(x, y))).map(_.value)) yield deviation) match {
      case deviations if deviations.nonEmpty =>
        val deviatingPixelCount = deviations.length
        PixelWiseColorDeviationMatchingResult(
          Deviation(deviations.sum / deviatingPixelCount),
          deviatingPixelCount
        )
      case _ => PixelWiseColorDeviationMatchingResult.withoutDeviation
    }
  }
}

object PixelWiseColorDeviationMatching {

  private final val DefaultDeviationCalculatorFactory: (ProcessingInput) => EuclideanDistanceCalculator =
    input => new EuclideanDistanceCalculator(input)

  def apply(): PixelWiseColorDeviationMatching =
    new PixelWiseColorDeviationMatching(DefaultDeviationCalculatorFactory)
  def apply(deviationCalculatorFactory: (ProcessingInput => NormalizedDeviationCalculator)): PixelWiseColorDeviationMatching =
    new PixelWiseColorDeviationMatching(deviationCalculatorFactory)
}

case class PixelWiseColorDeviationMatchingResult(deviation: Deviation,
                                                 deviantPixelCount: Int) extends MatchingResult

object PixelWiseColorDeviationMatchingResult {
  def withoutDeviation: PixelWiseColorDeviationMatchingResult = PixelWiseColorDeviationMatchingResult(NoDeviation, 0)
}
