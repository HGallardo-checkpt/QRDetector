/*
 * Copyright 2019-2020 by Security and Safety Things GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.securityandsafetythings.examples.helloworld.rest;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.SharedMemory;
import android.system.ErrnoException;
import com.securityandsafetythings.jumpsuite.commonhelpers.BitmapUtils;
import com.securityandsafetythings.jumpsuite.webhelpers.MimeType;
import com.securityandsafetythings.jumpsuite.webhelpers.annotations.Get;
import com.securityandsafetythings.jumpsuite.webhelpers.annotations.ProducedType;
import com.securityandsafetythings.jumpsuite.webhelpers.annotations.RequestPath;
import com.securityandsafetythings.jumpsuite.webhelpers.exceptions.WebException;
import com.securityandsafetythings.webserver.WebServerResponse;

import java.nio.ByteBuffer;

/**
 * Class responsible to receive API calls from the front end, process it, and return the result.
 *
 * The {@code @RequestPath} annotation denotes the REST path this class represents in the path hierarchy. At this point, 
 * the path relative to the base path of the api will be "api/example".
 */
@RequestPath("example")
public final class HelloWorldEndpoint {

    // Singleton instance of this class.
    private static HelloWorldEndpoint sInstance = null;
    private byte[] mBitmapBytes;

    /**
     * Private constructor for Singleton.
     */
    private HelloWorldEndpoint() { }

    /**
     * Gets an instance of this class.
     *
     * @return The Singleton instance of this class.
     */
    public static synchronized HelloWorldEndpoint getInstance() {
        // If no instance exists create one. Else return existing instance.
        if (sInstance == null) {
            sInstance = new HelloWorldEndpoint();
        }
        return sInstance;
    }

    /**
     * Returns a simple string to the front end to test the connection.
     *
     * The {@code ProducedType} annotation is used to indicate the correct MIME type
     * in the response as well as to send the returned value in the specified
     * type. If no type is specified, then this annotation will default to {@code MimeType.JSON}.
     * Here, {@code MimeType.TEXT} is being used to send the string as plain text.
     *
     * @return String which will be sent to the front end.
     */
    @Get
    @RequestPath("hello-world")
    @ProducedType(MimeType.TEXT)
    public static String helloWorld() {
        return "Hello, world inference";
    }

    /**
     * Sets the most recently received {@link Image} from the VideoPipeline.
     *
     * @param bitmap The {@code Image} retrieved from the VideoPipeline.
     */
    public synchronized void setImage(final Bitmap bitmap) {
        mBitmapBytes = BitmapUtils.compressBitmap(bitmap);
    }

    /**
     * Gets the most recent VideoPipeline {@link Image} as a byte[] and sends it as a {@link WebServerResponse} to the client.
     *
     * The {@code RequestPath} annotation denotes the REST path this method represents in the path hierarchy. At this point,
     * the path relative to the base path of the api will be "api/example/live".
     *
     * @return A {@code WebServerResponse} object that contains the {@link Bitmap} as a {@link SharedMemory}, if the bitmap exists.
     * @throws WebException If the bitmap does not exist or shared memory cannot be allocated.
     * This will be internally translated into a {@code WebServerResponse}
     * with a {@link WebServerResponse.ResponseStatus#NOT_FOUND} (i.e. a 404 status code) or
     * {@link WebServerResponse.ResponseStatus#INTERNAL_ERROR} (i.e. a 500 status code), and sent to the client.
     */
    @Get
    @RequestPath("live")
    public synchronized WebServerResponse getImage() throws WebException {
        if (mBitmapBytes == null) {
            throw new WebException.NotFoundBuilder().build();
        }
        // For large file data such as images, use a SharedMemory instance.
        try {
            final SharedMemory sharedMemory = SharedMemory.create("image", mBitmapBytes.length);
            final ByteBuffer byteBuffer = sharedMemory.mapReadWrite();
            byteBuffer.put(mBitmapBytes);
            return WebServerResponse.createSharedMemoryResponse(sharedMemory, WebServerResponse.ResponseStatus.OK, MimeType.JPEG);
        } catch (ErrnoException e) {
            throw new WebException.InternalServerErrorBuilder().build();
        }
    }
}
