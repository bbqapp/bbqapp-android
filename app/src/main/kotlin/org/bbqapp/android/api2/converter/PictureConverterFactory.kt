/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.api2.converter

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import org.bbqapp.android.api2.model.Picture
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class PictureConverterFactory private constructor() : Converter.Factory() {
    val IMAGE_JPEG_MEDIA_TYPE = MediaType.parse("image/jpeg")

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        if (isSupportedType(type)) {
            return PictureResponseConverter()
        }
        return super.responseBodyConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody> {
        if (isSupportedType(type)) {
            return PictureRequestConverter()
        }
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    fun isSupportedType(type: Type?): Boolean {
        return type is Class<*> && type.isAssignableFrom(Picture::class.java)
    }

    private inner class PictureRequestConverter : Converter<Picture, RequestBody> {
        @Throws(IOException::class)
        override fun convert(value: Picture) = PictureRequestBody(value)
    }

    private inner class PictureResponseConverter : Converter<ResponseBody, Picture> {

        @Throws(IOException::class)
        override fun convert(value: ResponseBody) = Picture(value.byteStream(), value.contentLength())
    }

    private inner class PictureRequestBody(private val picture: Picture) : RequestBody() {

        override fun contentType() = IMAGE_JPEG_MEDIA_TYPE

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            picture.out(sink.outputStream())
        }
    }

    companion object {
        fun create(): PictureConverterFactory {
            return PictureConverterFactory()
        }
    }
}

