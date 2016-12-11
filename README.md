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
val imageToBeChecked = ImageIO.read(new File("/home/user/image_to_be_checked.png"))
val referenceImage = ImageIO.read(new File("/home/user/reference_image.png"))

val scaling = Scaling(Dimension.square(640), ToBoundingBox)
val slicer = DefaultSlicer.withFixedSliceSizeOf(Dimension.square(4))
val matchingStrategy = PixelWiseColorDeviationMatching(DefaultDeviationCalculatorFactory)

val harmonization = HarmonizeResolutions(scaling)
val matcher = RegionalImageMatcher(slicer, matchingStrategy)

val result = Await.result(for {
  normalized <- harmonization(ProcessingInput(imageToBeChecked, referenceImage))
  matchingResult <- matcher(normalized)
} yield matchingResult, 30 seconds)

val outputStream = new FileOutputStream(new File("/home/user/difference.png"))

try {
  new DifferenceImageWriter(`png`).write(
    DifferenceImageData(
      result.processingInput,
      result.regionalMatchingResults
    ),
    outputStream
  )
} finally outputStream.close()
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
