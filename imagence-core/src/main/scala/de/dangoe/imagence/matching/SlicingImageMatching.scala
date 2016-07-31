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

import de.dangoe.imagence.common.{ProcessingInput, Prototype}
import de.dangoe.imagence.matching.ImageSlicing.ImplicitConversions._
import de.dangoe.imagence.matching.ImplicitConversions._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 23.07.16
  */
// TODO Draft to be tested or removed
@Prototype
class SlicingImageMatching[R <: MatchingResult] private(slicingStrategy: SlicingStrategy, matchingStrategy: MatchingStrategy[R])
                                                       (implicit executionContext: ExecutionContext, timeout: Duration) extends ((ProcessingInput) => (ProcessingInput, Map[Region, R])) {

  override def apply(processingInput: ProcessingInput): (ProcessingInput, Map[Region, R]) = {
    require(processingInput.image.dimension == processingInput.reference.dimension, "Image and reference image must be of same size!")

    val slices = Await.result(Future.sequence(processingInput.image.slice(slicingStrategy)), timeout)
    val referenceSlices = Await.result(Future.sequence(processingInput.reference.slice(slicingStrategy)), timeout)
    val slicePairs = for (i <- slices.indices) yield (slices(i), referenceSlices(i))

    (processingInput, Await.result(Future.sequence(for (partition <- slicePairs.grouped(Runtime.getRuntime.availableProcessors())) yield Future {
      processPartition(partition)
    }), timeout).flatten.toMap)
  }

  private def processPartition(slicePairs: Seq[(Slice, Slice)]): Seq[(Region, R)] = {
    for (slicePair <- slicePairs) yield slicePair._1.region -> matchingStrategy(ProcessingInput(slicePair._1, slicePair._2))
  }
}

object SlicingImageMatching {
  def apply[R <: MatchingResult](slicingStrategy: SlicingStrategy, matchingStrategy: MatchingStrategy[R])
                                (implicit executionContext: ExecutionContext, timeout: Duration): SlicingImageMatching[R] =
    new SlicingImageMatching[R](slicingStrategy, matchingStrategy)
}
