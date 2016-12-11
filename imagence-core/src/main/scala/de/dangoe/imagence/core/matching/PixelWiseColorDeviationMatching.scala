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
package de.dangoe.imagence.core.matching

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching.Deviation.NoDeviation
import de.dangoe.imagence.api.matching._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
class PixelWiseColorDeviationMatching private(deviationCalculatorFactory: (ProcessingInput => NormalizedDeviationCalculator))(implicit ec: ExecutionContext)
  extends BaseMatcher[PixelWiseColorDeviationMatchingResult] {

  private class DeviationAggregate {

    private var sum: Double = 0
    private var pixelCount: Int = 0

    def add(deviation: Deviation): Unit = {
      sum += deviation.value
      pixelCount = pixelCount + 1
    }

    def asMatchingResult: PixelWiseColorDeviationMatchingResult = if (pixelCount > 0) {
      PixelWiseColorDeviationMatchingResult(
        Deviation(sum / pixelCount),
        pixelCount
      )
    } else {
      PixelWiseColorDeviationMatchingResult.withoutDeviation
    }
  }

  override protected def applyInternal(input: ProcessingInput): Future[PixelWiseColorDeviationMatchingResult] = Future {
    val deviationCalculator = deviationCalculatorFactory(input)
    val imageSize = input.image.dimension
    val aggregate = new DeviationAggregate
    for (x <- 0 until imageSize.width;
         y <- 0 until imageSize.height;
         deviation <- deviationCalculator.calculate(input.image.getRGB(x, y), input.reference.getRGB(x, y)))
      yield aggregate.add(deviation)
    aggregate.asMatchingResult
  }
}

object PixelWiseColorDeviationMatching {

  final val DefaultDeviationCalculatorFactory: (ProcessingInput) => EuclideanDistanceCalculator = input => new EuclideanDistanceCalculator(input)

  def apply(deviationCalculatorFactory: (ProcessingInput => NormalizedDeviationCalculator))
           (implicit ec: ExecutionContext): PixelWiseColorDeviationMatching = new PixelWiseColorDeviationMatching(deviationCalculatorFactory)
}

case class PixelWiseColorDeviationMatchingResult(deviation: Deviation,
                                                 deviantPixelCount: Int) extends MatchingResult

object PixelWiseColorDeviationMatchingResult {
  def withoutDeviation: PixelWiseColorDeviationMatchingResult = PixelWiseColorDeviationMatchingResult(NoDeviation, 0)
}
