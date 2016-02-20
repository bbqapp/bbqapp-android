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

package org.bbqapp.android.api.model;

import android.net.Uri;

import org.bbqapp.android.api.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Picture implements ProgressListener {
    public static final int BUFFER_SIZE = 2048;

    private long length = -1;
    private InputStream inputStream;

    public Picture(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Picture(InputStream inputStream, long length) {
        this(inputStream);
        this.length = length;
    }

    public Picture(File file) throws FileNotFoundException {
        this(new FileInputStream(file), file.length());
    }

    public Picture(Uri file) throws FileNotFoundException {
        this(new File(file.toString()));
    }

    @Override
    public void onProgress(long contentLength, long transferred) {
    }

    protected final void onProgress(ProgressListener progressListener, long transferred) {
        if (progressListener != null) {
            progressListener.onProgress(length(), transferred);
        }
    }

    /**
     * Writes image data to {@link OutputStream}
     *
     * @param outputStream     data to write to
     * @param progressListener listener to notify about write progress
     * @throws IOException if stream is closed or other {@link IOException} occurred
     */
    public void out(OutputStream outputStream, ProgressListener progressListener) throws IOException {
        InputStream inputStream = in();
        try {
            int read;
            int transferred = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                outputStream.write(buffer, 0, read);
                outputStream.flush();
                transferred += read;
                onProgress(progressListener, transferred);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Writes image data to {@link OutputStream}
     *
     * @param outputStream data to write to
     * @throws IOException if stream is closed or other {@link IOException} occurred
     */
    public void out(OutputStream outputStream) throws IOException {
        out(outputStream, this);
    }

    /**
     * Returns {@link InputStream} of image data
     *
     * @return input stream
     */
    public InputStream in() throws IOException {
        return inputStream;
    }

    /**
     * Returns image length
     *
     * @return length
     */
    public long length() {
        return length;
    }
}
