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

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.matching._

import scala.concurrent.{ExecutionContext, Future}

class RegionalImageMatcher[R <: MatchingResult] private(slicer: Slicer, matcher: Matcher[R])(implicit ec: ExecutionContext) extends BaseMatcher[RegionalImageMatcherResult[R]] {

  override def applyInternal(input: ProcessingInput): Future[RegionalImageMatcherResult[R]] =
    process(input).map(RegionalImageMatcherResult(input, _))

  private def process(input: ProcessingInput) = {
    Future.sequence {
      for (pair <- slicer.slice(input.image) zip slicer.slice(input.reference))
        yield processPartition(pair)
    }
  }

  @inline private def processPartition(slicesToBeCompared: Future[(Slice, Slice)]): Future[RegionalMatchingResult[R]] = {
    import de.dangoe.imagence.api.Implicits._

    for {
      pair <- slicesToBeCompared
      matchingResult <- matcher(ProcessingInput(pair._1, pair._2))
    } yield RegionalMatchingResult(pair._1.region, matchingResult)
  }

  private implicit def reducePairOfFuturesOfSameType[T](pair: (Future[T], Future[T]))
                                                       (implicit ec: ExecutionContext): Future[(T, T)] = {
    Future.sequence(Seq(pair._1, pair._2)).map(seq => (seq.head, seq.last))
  }
}

object RegionalImageMatcher {
  def apply[R <: MatchingResult](slicer: Slicer, matcher: Matcher[R])
                                (implicit ec: ExecutionContext): RegionalImageMatcher[R] =
    new RegionalImageMatcher[R](slicer, matcher)
}

case class RegionalImageMatcherResult[R <: MatchingResult](processingInput: ProcessingInput, regionalMatchingResults: Seq[RegionalMatchingResult[R]])
  extends MatchingResult {

  import de.dangoe.imagence.api.Implicits._

  import RegionalImageMatcherResult._

  override def deviation: Deviation = {
    val averageDeviation = if (regionalMatchingResults.nonEmpty) {
      regionalMatchingResults.map(r => r.deviation.value).sum / regionalMatchingResults.length
    } else 0
    val inputArea = calculateArea(processingInput.image)
    val matchingResultsArea = regionalMatchingResults.map(r => calculateArea(r.region)).sum
    Deviation(averageDeviation * matchingResultsArea / inputArea)
  }
}

object RegionalImageMatcherResult {

  import scala.language.reflectiveCalls

  private def calculateArea(obj: {def dimension: Dimension}): Double = obj.dimension.width * obj.dimension.height
}
