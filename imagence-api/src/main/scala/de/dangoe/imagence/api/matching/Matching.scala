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
package de.dangoe.imagence.api.matching

import de.dangoe.imagence.api.Implicits._
import de.dangoe.imagence.api.ProcessingInput

import scala.concurrent.Future

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 31.07.16
  */
trait MatchingResult {
  def deviation: Deviation
}

trait Regional {
  def region: Region
}

case class RegionalMatchingResult[R <: MatchingResult](region: Region, delegate: R) extends MatchingResult with Regional {
  override def deviation: Deviation = delegate.deviation
}

trait Matcher[R <: MatchingResult] extends (ProcessingInput => Future[R])

abstract class BaseMatcher[R <: MatchingResult] extends Matcher[R] {

  final def apply(input: ProcessingInput): Future[R] = {
    if (input.image.dimension != input.reference.dimension) {
      return Future.failed(MatchingNotPossible("Image dimension differs from reference image!"))
    }
    applyInternal(input)
  }

  protected def applyInternal(input: ProcessingInput): Future[R]
}

case class MatchingNotPossible(message: String) extends RuntimeException(message)

trait NormalizedDeviationCalculator {
  def calculate(rgb: Int, referenceRgb: Int): Option[Deviation]
}

case class Deviation(value: Double) {
  require(value >= 0, "Value must not be smaller than zero.")
  require(value <= 1, "Value must not be larger than one.")
}

object Deviation {
  val NoDeviation = Deviation(0)
  val MaximumDeviation = Deviation(1)
}

object Implicits {
  implicit def deviationOrdering: Ordering[Deviation] = Ordering.fromLessThan(_.value < _.value)
}
