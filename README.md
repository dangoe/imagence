# imagence - an image difference analysis library

[![Build Status](https://travis-ci.org/dangoe/imagence.svg?branch=develop)](https://travis-ci.org/dangoe/imagence)

## What it is

This library provides comparison methods to find regional differences between an image and a given reference (i.e. check a generated image for errors on the basis of an existing reference image).

## What it is not

It does not provide any kind of pattern recognition or pattern similarity analysis methods.

## Core features

* Regional image matching to identify differing regions. 
* Difference image visualization.
* A flexible API that supports linkable preprocessors and matchers.
* Full multi-core support.
* Build-in PDF conversion support for PDF comparison.

## Example usage

```scala
package de.dangoe.imagence.core

import java.awt.image.BufferedImage
import java.io.{File, FileOutputStream}
import javax.imageio.ImageIO

import de.dangoe.imagence.api.ProcessingInput
import de.dangoe.imagence.api.io.DifferenceImageData
import de.dangoe.imagence.api.matching.{Deviation, Dimension}
import de.dangoe.imagence.api.preprocessing.{Conversion, Preprocessor}
import de.dangoe.imagence.api.util.Done
import de.dangoe.imagence.core.io._
import de.dangoe.imagence.core.matching.PixelWiseColorDeviationMatching.DefaultDeviationCalculatorFactory
import de.dangoe.imagence.core.matching.{DefaultSlicer, PixelWiseColorDeviationMatching, PixelWiseColorDeviationMatchingResult, RegionalImageMatcher}
import de.dangoe.imagence.core.preprocessing.{GaussianBlur, HarmonizeResolutions, Scaling, ScalingQuality}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.reflectiveCalls

object TestApp {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val scalingQuality = ScalingQuality.Low

  def main(args: Array[String]): Unit = {
    val imageToBeChecked: BufferedImage = ImageIO.read(new File("/home/user/image_to_be_checked.png"))
    val referenceImage: BufferedImage = ImageIO.read(new File("/home/user/reference_image.png"))

    val slicer: DefaultSlicer = DefaultSlicer.withFixedSliceSizeOf(Dimension.square(8))
    val matchingStrategy: PixelWiseColorDeviationMatching = PixelWiseColorDeviationMatching(DefaultDeviationCalculatorFactory)

    val input: ProcessingInput = ProcessingInput(imageToBeChecked, referenceImage)

    val harmonization: Conversion[ProcessingInput] = HarmonizeResolutions(Scaling.toBoundingBox(Dimension.square(2000)))
    val gaussianBlur: Conversion[ProcessingInput] = Preprocessor.fromSingleImageConversion(GaussianBlur(4))

    val imageMatcher: RegionalImageMatcher[PixelWiseColorDeviationMatchingResult] = RegionalImageMatcher(slicer, matchingStrategy)

    val eventualMatchingResult: Future[Done] = {
      for {
        harmonized <- harmonization(input)
        blurred <- gaussianBlur(harmonized)
        matcherResult <- imageMatcher(blurred)
        differenceImageWriter = new SimpleDifferenceImageWriter(ImageFormat.`png`)
        writingResult <- use(new FileOutputStream(new File("/home/user/difference.png"))) { outputStream =>
          differenceImageWriter.write(
            DifferenceImageData(
              harmonized,
              matcherResult.regionalMatchingResults.filter(_.deviation > Deviation(0.3))
            ),
            outputStream
          )
        }
      } yield writingResult
    }

    Await.result(eventualMatchingResult, 15.seconds)
  }

  private def use[T, C <: {def close() : Unit}](closeable: => C)(fun: C => Future[T]): Future[T] = {
    val closeableInstance = closeable
    val eventualResult = fun(closeableInstance)
    eventualResult.onComplete(_ => closeableInstance.close())
    eventualResult
  }
}
```

## Contributing

Please fork the repository, if you like to. Pull requests are very welcome.

## License

Copyright 2016 Daniel Götten

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
>
>     http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.
