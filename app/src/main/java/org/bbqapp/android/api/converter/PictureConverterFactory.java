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

package org.bbqapp.android.api.converter;

import org.bbqapp.android.api.model.Picture;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class PictureConverterFactory extends Converter.Factory {
    public static final MediaType IMAGE_JPEG_MEDIA_TYPE = MediaType.parse("image/jpeg");

    public static PictureConverterFactory create() {
        return new PictureConverterFactory();
    }

    private PictureConverterFactory() {
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (isSupportedType(type)) {
            return new PictureResponseConverter();
        }
        return super.responseBodyConverter(type, annotations, retrofit);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (isSupportedType(type)) {
            return new PictureRequestConverter();
        }
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    public boolean isSupportedType(Type type) {
        return type instanceof Class && ((Class<?>) type).isAssignableFrom(Picture.class);
    }

    private class PictureRequestConverter implements Converter<Picture, RequestBody> {
        @Override
        public RequestBody convert(Picture value) throws IOException {
            return new PictureRequestBody(value);
        }
    }

    private class PictureResponseConverter implements Converter<ResponseBody, Picture> {

        @Override
        public Picture convert(ResponseBody value) throws IOException {
            return new Picture(value.byteStream(), value.contentLength());
        }
    }

    private class PictureRequestBody extends RequestBody {
        private Picture picture;

        public PictureRequestBody(Picture picture) {
            this.picture = picture;
        }

        @Override
        public MediaType contentType() {
            return IMAGE_JPEG_MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            picture.out(sink.outputStream());
        }
    }
}
