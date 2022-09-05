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

package com.securityandsafetythings.examples.helloworld.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.rest.HelloWorldEndpoint;
import com.securityandsafetythings.examples.helloworld.storage.StorageBitmapLocally;

/**
 * Class responsible for handling the messages sent to the BitmapHandlerThread.
 * <p> It handles the following message:
 * <ol>
 *   <li> {@link Message#SET_BITMAP} - In the {@link HelloWorldEndpoint}, sets the {@link Bitmap} object
 *   attached to the {@link android.os.Message}. </li>
 * </ol>
 */
public class BitmapHandler extends Handler {

    private static final String LOGTAG = BitmapHandler.class.getSimpleName();
    private Context context;

    /**
     * Constructs a BitmapHandler object.
     *
     * @param looper The {@code Looper} associated with the BitmapHandlerThread.
     */
    public BitmapHandler(final Looper looper) {
        super(looper);

    }

    /**
     * Acquires the latest bitmap and sets it in the {@link HelloWorldEndpoint}.
     *
     * @param msg The message containing a {@link Bitmap} received from the VideoPipeline.
     */
    @Override
    public void handleMessage(final android.os.Message msg) {
        final Message messageType = Message.fromOrdinal(msg.what);

        switch (messageType) {
        case SET_BITMAP:

            Bitmap currentBitmap = (Bitmap)msg.obj;
            HelloWorldEndpoint.getInstance().setImage(RotateBitmap(currentBitmap,180));
             break;
        default:
            Log.e(LOGTAG, "Unknown message received on BitmapHandlerThread");
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Enum defining the messages that the BitmapHandlerThread can process.
     */
    public enum Message {
        /**
         * Use this message to set the bitmap.
         */
        SET_BITMAP;

        private static final Message[] VALUES = values();

        /**
         * Retrieves the Message value given an ordinal value.
         *
         * @param ordinal The ordinal value representation of a {@code Message}.
         * @return A {@code Message} represented by the provided ordinal value.
         */
        private static Message fromOrdinal(final int ordinal) {
            return VALUES[ordinal];
        }
    }
}