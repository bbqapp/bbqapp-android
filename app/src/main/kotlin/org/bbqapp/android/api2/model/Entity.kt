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

package org.bbqapp.android.api2.model

import android.net.Uri
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.*

import org.bbqapp.android.extension.copyTo;

@JsonIgnoreProperties(ignoreUnknown = true)
interface Entity

open class Id(
        @JsonProperty("_id") val id: String? = null) : Entity

class Address : Entity

class Location(
        val coordinates: List<Double>, val type: String) : Entity

class Comment(val comment:String,
              val score: Int) : Entity

class Place(
        @JsonProperty("_id") id: String? = null,
        val tags: List<String>? = null,
        val address: Address? = null,
        val location: Location,
        val comments: List<Comment>? = null) : Id(id)

class PictureInfo(
        @JsonProperty("_id") id: String,
        @JsonProperty("_place") val placeId: String,
        val meta: PictureMeta) : Id(id)

class PictureMeta(
        val mimeType: String,
        val url: String) : Entity

class Picture(val stream: InputStream, val length: Long = -1) {
    constructor(file: File) : this(FileInputStream(file), file.length())
    constructor(uri: Uri) : this(File(uri.toString()))

    @Throws(IOException::class)
    fun out(out: OutputStream, progress: ((Long, Long) -> Unit)? = null): Long {
        try {
            return stream.copyTo(out, {t: Long -> progress?.let { progress(length, t) }})
        } finally {
            try {
                stream.close()
            } catch (ignored: IOException) {
            }
        }
    }
}