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
package de.dangoe.imagence.api

import java.awt.image.BufferedImage

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Daniel Götten <daniel.goetten@googlemail.com>
  * @since 30.07.16
  */
package object preprocessing {

  trait Conversion[T] {
    def apply(value: T)(implicit ec: ExecutionContext): Future[T]
  }

  object Implicits {
    implicit def toPreprocessor(conv: Conversion[BufferedImage]): Preprocessor = Preprocessor(conv)
  }

  class Preprocessor private(conv: Conversion[BufferedImage]) extends Conversion[ProcessingInput] {

    override def apply(input: ProcessingInput)(implicit ec: ExecutionContext) = {
      for {
        processedImage <- conv(input.image)
        processedReference <- conv(input.reference)
      } yield ProcessingInput(processedImage, processedReference)
    }
  }

  object Preprocessor {
    def apply(conv: Conversion[BufferedImage]): Preprocessor = new Preprocessor(conv)
  }
}
