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

package org.bbqapp.android.api.model

import android.net.Uri
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.*

import org.bbqapp.android.extension.copyTo;
import timber.log.Timber

@JsonIgnoreProperties(ignoreUnknown = true)
interface Entity

interface HasId : Entity {
    val id: String?
}

data class Id(@JsonProperty("_id") override val id: String? = null) : HasId

data class Address(val country: String) : Entity

data class Location(
        val coordinates: List<Double>, val type: String) : Entity

data class Comment(val comment: String,
                   val score: Int) : Entity

data class Place(
        @JsonProperty("_id") override val id: String? = null,
        val tags: List<String>? = null,
        val address: Address? = null,
        val location: Location,
        val comments: List<Comment>? = null) : HasId

data class PictureInfo(
        @JsonProperty("_id") override val id: String,
        @JsonProperty("_place") val placeId: String,
        val meta: PictureMeta) : HasId

data class PictureMeta(
        val mimeType: String?,
        val url: String) : Entity

data class Picture(val input: InputStream,
                   val length: Long = -1,
                   val progress: ((Long, Long) -> Unit)? = null) {
    constructor(file: File,
                progress: ((Long, Long) -> Unit)? = null
    ) : this(FileInputStream(file), file.length(), progress)

    @Throws(FileNotFoundException::class)
    constructor(uri: Uri,
                progress: ((Long, Long) -> Unit)? = null
    ) : this(File(uri.toString()), progress)

    @Throws(IOException::class)
    fun out(out: OutputStream, progress: ((Long, Long) -> Unit)? = null): Long {
        try {
            val listener = fun(transferred: Long): Unit {
                progress?.invoke(length, transferred)
                this.progress?.invoke(length, transferred)
            }
            return input.copyTo(out, listener)
        } finally {
            try {
                input.close()
            } catch (e: IOException) {
                Timber.w(e, "Could not close input stream")
            }
        }
    }
}