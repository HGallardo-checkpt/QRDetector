package com.securityandsafetythings.examples.helloworld.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.RequiresApi;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.securityandsafetythings.Build;
import com.securityandsafetythings.app.VideoService;
import com.securityandsafetythings.examples.helloworld.BuildConfig;
import com.securityandsafetythings.examples.helloworld.direction.DirectionDetection;
import com.securityandsafetythings.examples.helloworld.direction.DirectionDetectorHandler;
import com.securityandsafetythings.examples.helloworld.events.OnDetectionProcessEvent;
import com.securityandsafetythings.examples.helloworld.events.OnFinishDetectionEvent;
import com.securityandsafetythings.examples.helloworld.events.OnPostProccessingCompletedEvent;
import com.securityandsafetythings.examples.helloworld.events.OnPostProccessingNotDetectedEvent;
import com.securityandsafetythings.examples.helloworld.inference.handlers.InferenceHandler;
import com.securityandsafetythings.examples.helloworld.render.RenderHandler;
import com.securityandsafetythings.examples.helloworld.rest.QRDetectionEndPoint;
import com.securityandsafetythings.examples.helloworld.utilities.BitmapHandler;
import com.securityandsafetythings.jumpsuite.commonhelpers.BitmapUtils;
import com.securityandsafetythings.jumpsuite.webhelpers.WebServerConnection;
import com.securityandsafetythings.video.RefreshRate;
import com.securityandsafetythings.video.VideoCapture;
import com.securityandsafetythings.video.VideoManager;
import com.securityandsafetythings.video.VideoSession;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainService extends VideoService {

    private static final String INFERENCE_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "InferenceThread");
    private static final String RENDER_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "RenderThread");

    private static final String BITMAP_THREAD_NAME = String.format("%s%s",
            BitmapHandler.class.getSimpleName(), "BitmapThread");

    private static final String DIRECTION_DETECTOR_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "DetectorThread");

    private static final String LOGTAG = MainService.class.getSimpleName();

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    private static final Object INFERENCE_HANDLER_LOCK =new Object();
    private static final Object BITMAP_HANDLER_LOCK = new Object();
    private static final Object RENDER_HANDLER_LOCK = new Object();
    private static final Object DIRECTION_DETECTOR_HANDLER_LOCK = new Object();

    private VideoManager mVideoManager;
    private WebServerConnection mWebServerConnection;
    private Handler mBitmapHandler;
    private HandlerThread mBitmapHandlerThread;

    private InferenceHandler mInferenceHandler;
    private HandlerThread mInferenceHandlerThread;

    private Handler mRenderHandler;
    private HandlerThread mRenderHandlerThread;


    private Handler mDirectorDetectorHandler;

    private HandlerThread mDirectionDetectorThread;
    private VideoCapture mCapture;

    private List<DirectionDetection> directionDetectionList;
    private int detectionCounter = 0;
    private boolean detectedCode = false;


    @Override
    public void onCreate() {
        super.onCreate();
        attachWebServer();
        directionDetectionList = new ArrayList<>();
        EventBus.getDefault().register(this);

     }

    @Override
    public void onDestroy() {
        detachWebServer();
        EventBus.getDefault().unregister(this);
        stopBitmapHandlerThread();
        stopInferenceThread();
        stopDirectionDetectorThread();

        super.onDestroy();
    }


    @Override
    protected void onVideoAvailable(final VideoManager manager) {
        mVideoManager = manager;
        mCapture = mVideoManager.getDefaultVideoCapture();
        startBitmapHandlerThread();
        startInferenceThread();
        configureDetector();
        startVideoSession();
        startDirectionDetectorThread();
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.ECLAIR)
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final OnPostProccessingCompletedEvent onPostProccessingCompletedEvent) {

        Pair<Bitmap, List<Barcode>> detectionResult = onPostProccessingCompletedEvent.getResult();
        Log.e("QR: ",""+detectionResult.second.get(0).getRawValue());
        DirectionDetection directionDetection = new DirectionDetection();
        directionDetection.translate = detectionResult.second.get(0).getRawValue();

        directionDetection.center = new Point(
                (detectionResult.first.getWidth()/2),
                (detectionResult.first.getHeight()/2));
        directionDetection.position = new Point(
                detectionResult.second.get(0).getBoundingBox().left,
                detectionResult.second.get(0).getBoundingBox().top);

        this.directionDetectionList.add(directionDetection);
        detectionCounter ++;

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(final OnPostProccessingNotDetectedEvent onPostProccessingNotDetectedEvent) {
        if(onPostProccessingNotDetectedEvent.getResult()){
             if( detectionCounter >= 2){
                synchronized (DIRECTION_DETECTOR_HANDLER_LOCK) {
                    if (!mDirectorDetectorHandler.hasMessages(DirectionDetectorHandler.Message.CALCULATE_DIRECTION.ordinal())) {
                        List<DirectionDetection> copy = directionDetectionList.stream().collect(Collectors.toList());
                        mDirectorDetectorHandler.obtainMessage(DirectionDetectorHandler.Message.CALCULATE_DIRECTION.ordinal(),
                                copy).sendToTarget();
                        detectionCounter = 0;
                        directionDetectionList.clear();


                    }
                }
            }

        }

    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(final OnFinishDetectionEvent onFinishDetectionEvent) {

        String direction = onFinishDetectionEvent.getResult();
     //   detectionCounter = 0;
     //   directionDetectionList.clear();
        Log.e("DIRECTION DETECTED; ",direction);

    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(final OnDetectionProcessEvent onDetectionProcessEvent) {

        Bitmap bitmap = onDetectionProcessEvent.getResult();
        if (!mInferenceHandler.hasMessages(InferenceHandler.Message.RUN_INFERENCE.ordinal())) {
            mInferenceHandler.obtainMessage(InferenceHandler.Message.RUN_INFERENCE.ordinal(),bitmap).sendToTarget();
        }

    }

    private void configureDetector() {
        if (!mInferenceHandler.hasMessages(InferenceHandler.Message.CONFIGURE_DETECTOR.ordinal())) {
            mInferenceHandler.obtainMessage(InferenceHandler.Message.CONFIGURE_DETECTOR.ordinal()).sendToTarget();
        }

    }



    @Override
    protected void onImageAvailable(final ImageReader reader) {
        try (Image image = reader.acquireLatestImage()) {
            if (image == null) {
                Log.e(LOGTAG, "onImageAvailable(): ImageReader returned null image.");
                return;
            }


            synchronized (BITMAP_HANDLER_LOCK) {
                 if (mBitmapHandler != null && !mBitmapHandler.hasMessages(BitmapHandler.Message.SET_BITMAP.ordinal())) {
                     mBitmapHandler.obtainMessage(BitmapHandler.Message.SET_BITMAP.ordinal(), BitmapUtils.imageToBitmap(image))
                            .sendToTarget();
                }
            }
        }
    }

    @Override
    @SuppressWarnings("MagicNumber")
    protected void onVideoClosed(final VideoSession.CloseReason reason) {
        Log.d(LOGTAG, "onVideoClosed(): reason " + reason.name());
        if (Build.VERSION.MAX_API >= 5) {
            if (reason == VideoSession.CloseReason.BASE_CAMERA_CONFIGURATION_CHANGED) {
                Log.d(LOGTAG, "onVideoClosed(): Triggering the restart of the VideoSession that got closed due to " + reason.name());
                UI_HANDLER.post(this::startVideoSession);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void attachWebServer() {
        mWebServerConnection = new WebServerConnection.Builder(
                this,
                BuildConfig.WEBSITE_ASSET_PATH,
                BuildConfig.REST_PATH_PREFIX,
                QRDetectionEndPoint.getInstance()
        ).build();

        mWebServerConnection.open();
    }



    @SuppressLint("MissingPermission")
    private void detachWebServer() {
        if (mWebServerConnection != null) {
            mWebServerConnection.close();
        }
    }

    @SuppressWarnings("MagicNumber")
    private void startVideoSession() {
        final VideoCapture capture = mVideoManager.getDefaultVideoCapture();
        final double aspectRatio = (double)capture.getWidth() / capture.getHeight();
        int requestWidth = capture.getWidth();
        int requestHeight = capture.getHeight();
        if (capture.getHeight() > 1080) {
            requestHeight = 1080;
            requestWidth = (int)(aspectRatio * requestHeight);
        }

        openVideo(capture, requestWidth, requestHeight, RefreshRate.LIVE, false);
        Log.d(LOGTAG, "startVideoSession(): openVideo() is called and VideoSession is started");
    }

    private void startRenderThread(){
        mRenderHandlerThread = new HandlerThread(RENDER_THREAD_NAME);
        mRenderHandlerThread.start();
        mRenderHandler = new RenderHandler(mRenderHandlerThread.getLooper());

    }
    private void stopRenderThread(){
        synchronized (RENDER_HANDLER_LOCK) {
            mRenderHandlerThread = null;
            if (mRenderHandlerThread != null) {
                mRenderHandlerThread.quitSafely();
                mRenderHandlerThread = null;
            }
        }
    }
    private void startInferenceThread() {
        mInferenceHandlerThread = new HandlerThread(INFERENCE_THREAD_NAME);
        mInferenceHandlerThread.start();
        mInferenceHandler = new InferenceHandler(mInferenceHandlerThread.getLooper());

    }
    private void stopInferenceThread() {
        synchronized (INFERENCE_HANDLER_LOCK) {
            mInferenceHandler = null;
            if (mInferenceHandlerThread != null) {
                mInferenceHandlerThread.quitSafely();
                mInferenceHandlerThread = null;
            }
        }
    }
    private void startBitmapHandlerThread() {
        mBitmapHandlerThread = new HandlerThread(BITMAP_THREAD_NAME);
        mBitmapHandlerThread.start();
        mBitmapHandler = new BitmapHandler(mBitmapHandlerThread.getLooper());
    }

    private void stopBitmapHandlerThread() {
        // Use the lock to prevent sending messages on a dead thread.
        synchronized (BITMAP_HANDLER_LOCK) {
            mBitmapHandler = null;
            if (mBitmapHandlerThread != null) {
                mBitmapHandlerThread.quitSafely();
                mBitmapHandlerThread = null;
            }
        }
    }


    private void startDirectionDetectorThread(){
        mDirectionDetectorThread = new HandlerThread(DIRECTION_DETECTOR_THREAD_NAME);
        mDirectionDetectorThread.start();
        mDirectorDetectorHandler = new DirectionDetectorHandler(mDirectionDetectorThread.getLooper());

    }
    private void stopDirectionDetectorThread(){

        synchronized (DIRECTION_DETECTOR_HANDLER_LOCK) {
            mDirectorDetectorHandler = null;
            if (mDirectionDetectorThread != null) {
                mDirectionDetectorThread.quitSafely();
                mDirectionDetectorThread = null;
            }
        }
    }
}
