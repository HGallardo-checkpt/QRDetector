package com.securityandsafetythings.examples.helloworld.rest;
import android.graphics.Bitmap;
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

@RequestPath("example")
public class QRDetectionEndPoint {
    private static QRDetectionEndPoint instance = null;
    private byte[] bitmapBytes;
    private QRDetectionEndPoint(){}

    public static synchronized QRDetectionEndPoint getInstance() {
        if (instance == null) {
            instance = new QRDetectionEndPoint();
        }
        return instance;
    }


    public synchronized void setImage(final Bitmap bitmap) {
        bitmapBytes = BitmapUtils.compressBitmap(bitmap);
    }

    @Get
    @RequestPath("live")
    public synchronized WebServerResponse getImage() throws WebException {
        if (bitmapBytes == null) {
            throw new WebException.NotFoundBuilder().build();
        }
        try {
            final SharedMemory sharedMemory = SharedMemory.create("image", bitmapBytes.length);
            final ByteBuffer byteBuffer = sharedMemory.mapReadWrite();
            byteBuffer.put(bitmapBytes);
            return WebServerResponse.createSharedMemoryResponse(sharedMemory, WebServerResponse.ResponseStatus.OK, MimeType.JPEG);
        } catch (ErrnoException e) {
            throw new WebException.InternalServerErrorBuilder().build();
        }
    }

}
