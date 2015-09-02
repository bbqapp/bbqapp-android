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

package org.bbqapp.android.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.MediaType;

import org.bbqapp.android.api.model.Picture;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Converter for API calls. Can convert reposes to objects or to binary picture data and vise versa
 */
class ApiConverter implements Converter {
    private Gson gson;

    ApiConverter() {
        gson = new GsonBuilder().create();
    }

    private static final String PREFERRED_CHARSET_NAME = "utf-8";
    public static final Charset DEFAULT_CHARSET =
            Charset.isSupported(PREFERRED_CHARSET_NAME) ? Charset.forName(PREFERRED_CHARSET_NAME) : Charset.defaultCharset();
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=" +
            DEFAULT_CHARSET.name().toLowerCase());
    public static final MediaType IMAGE_JPEG_MEDIA_TYPE = MediaType.parse("image/jpeg");

    // TODO remove after fix
    public static final MediaType IMAGE_JPG_MEDIA_TYPE = MediaType.parse("image/jpg");

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        MediaType mediaType = MediaType.parse(body.mimeType());

        if (JSON_MEDIA_TYPE.equals(mediaType)) {
            Charset charset = mediaType.charset(DEFAULT_CHARSET);
            return fromJson(body, type, charset);
        } else if (IMAGE_JPEG_MEDIA_TYPE.equals(mediaType) || IMAGE_JPG_MEDIA_TYPE.equals(mediaType)) {
            return fromPicture(body);
        } else {
            throw new ConversionException("Unsupported media type " + mediaType);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        if (object instanceof Picture) {
            return new PictureTypedOutput((Picture) object);
        } else {
            return new JsonTypedOutput(object);
        }
    }

    private Object fromPicture(TypedInput body) throws ConversionException {
        try {
            return new Picture(body.in(), body.length());
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    private Object fromJson(TypedInput body, Type type, Charset charset) throws ConversionException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(body.in(), charset);
            return gson.fromJson(isr, type);
        } catch (IOException e) {
            throw new ConversionException(e);
        } catch (JsonParseException e) {
            throw new ConversionException(e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Used for uploading image
     */
    private class PictureTypedOutput implements TypedOutput {
        private Picture picture;

        PictureTypedOutput(Picture picture) {
            this.picture = picture;
        }

        @Override
        public String fileName() {
            return null;
        }

        @Override
        public String mimeType() {
            return IMAGE_JPEG_MEDIA_TYPE.toString();
        }

        @Override
        public long length() {
            return picture.length();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            picture.out(out);
        }
    }

    /**
     * Used to upload json content
     */
    private class JsonTypedOutput implements TypedOutput {
        private Object object;

        JsonTypedOutput(Object object) {
            this.object = object;
        }

        @Override
        public String fileName() {
            return null;
        }

        @Override
        public String mimeType() {
            return JSON_MEDIA_TYPE.toString();
        }

        @Override
        public long length() {
            return -1;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            // out is already buffered
            gson.toJson(object, new Appendable() {
                @Override
                public Appendable append(char c) throws IOException {
                    return append(String.valueOf(c));
                }

                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    return write(String.valueOf(csq).getBytes(DEFAULT_CHARSET));
                }

                @Override
                public Appendable append(CharSequence csq, int start, int end) throws IOException {
                    return append(csq.subSequence(start, end));
                }

                private Appendable write(byte[] bytes) throws IOException {
                    out.write(bytes);
                    return this;
                }
            });
        }
    }
}
