package com.securityandsafetythings.examples.helloworld.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.securityandsafetythings.examples.helloworld.api.APIClient;
import com.securityandsafetythings.examples.helloworld.api.APIInterface;
import com.securityandsafetythings.examples.helloworld.direction.DirectionDetection;
import com.securityandsafetythings.examples.helloworld.direction.DirectionDetectorHandler;
import com.securityandsafetythings.examples.helloworld.events.OnDetectionProcessEvent;
import com.securityandsafetythings.examples.helloworld.events.OnPostProccessingCompletedEvent;
import com.securityandsafetythings.examples.helloworld.inference.handlers.InferenceHandler;
import com.securityandsafetythings.examples.helloworld.events.OnInferenceCompletedEvent;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;
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

public class MainService extends VideoService {

    private static final String INFERENCE_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "InferenceThread");
    private static final String RENDER_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "InferenceThread");

    private static final String BITMAP_THREAD_NAME = String.format("%s%s",
            BitmapHandler.class.getSimpleName(), "BitmapThread");

    private static final String DIRECTION_DETECTOR_THREAD_NAME = String.format("%s%s",
            MainService.class.getSimpleName(), "DetectorThread");

    private static final String LOGTAG = MainService.class.getSimpleName();
    /*
     * When the VideoSession is restarted due to base camera configuration changes,
     * this Handler is used to post messages to the UI thread/main thread for proper rendering.
     */
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    /**
     * Lock object for making sure that the {@link BitmapHandler} isn't used to send messages while the thread is being stopped.
     *
     * @see #onImageAvailable(ImageReader)
     */
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

    /**
     * This callback is triggered when the VideoPipeline is available to begin capturing images.
     *
     * @param manager The {@code VideoManager} object that is used to obtain access to the VideoPipeline.
     */
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
        directionDetection.w = detectionResult.first.getWidth();
        directionDetection.h = detectionResult.first.getHeight();
        directionDetection.position = new Pair(
                detectionResult.second.get(0).getBoundingBox().top,
                detectionResult.second.get(0).getBoundingBox().right);

        directionDetectionList.add(directionDetection);
        detectionCounter ++;
        if(detectionCounter > 10){
            Log.e("LAUNCH POSITION DETECTOR","---->");
            synchronized (DIRECTION_DETECTOR_HANDLER_LOCK) {
                if (!mDirectorDetectorHandler.hasMessages(DirectionDetectorHandler.Message.CALCULATE_DIRECTION.ordinal())) {
                    mDirectorDetectorHandler.obtainMessage(DirectionDetectorHandler.Message.CALCULATE_DIRECTION.ordinal(),
                            directionDetectionList).sendToTarget();
                    detectionCounter = 0;
                }
            }


        }
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
            // ImageReader may sometimes return a null image.
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
/*

            synchronized (INFERENCE_HANDLER_LOCK) {
                if (!mInferenceHandler.hasMessages(InferenceHandler.Message.RUN_INFERENCE.ordinal())) {
                    mInferenceHandler.obtainMessage(InferenceHandler.Message.RUN_INFERENCE.ordinal(),
                            BitmapUtils.imageToBitmap(image)).sendToTarget();
                }

            }
*/

        }
    }

    @Override
    @SuppressWarnings("MagicNumber")
    protected void onVideoClosed(final VideoSession.CloseReason reason) {
        Log.d(LOGTAG, "onVideoClosed(): reason " + reason.name());
        /*
         * In API level v5 and above, VideoSession.CloseReason.BASE_CAMERA_CONFIGURATION_CHANGED was
         * introduced to indicate that the VideoPipeline configuration (for example, camera is rotated) has been changed.
         * In these situations, it is recommended to restart the VideoSession to provide seamless user experience.
         */
        if (Build.VERSION.MAX_API >= 5) {
            if (reason == VideoSession.CloseReason.BASE_CAMERA_CONFIGURATION_CHANGED) {
                Log.d(LOGTAG, "onVideoClosed(): Triggering the restart of the VideoSession that got closed due to " + reason.name());
                // For proper rendering, it is important to restart the VideoSession in the main thread.
                UI_HANDLER.post(this::startVideoSession);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void attachWebServer() {
        // Create the app's web server extension by building a WebServerConnection.
        mWebServerConnection = new WebServerConnection.Builder(
                this,
                // BuildConfig variables are set in app/build.gradle
                /*
                 * Set the path to the subfolder inside src/main/assets that contains static files (e.g. website files) to be served.
                 * Current value: "website"
                 * Result: The files in src/main/assets/website will be served to clients.
                 */
                BuildConfig.WEBSITE_ASSET_PATH,
                /*
                 * Set the path prefix for REST API endpoints (such as the endpoints in HelloWorldEndpoint).
                 * Current value: "api"
                 * Result: API request URLs will start with "/app/com.securityandsafetythings.examples.helloworld/api/"
                 */
                BuildConfig.REST_PATH_PREFIX,
                /*
                 * Set a class instance that contains REST API endpoints.
                 * Current value: HelloWorldEndpoint.getInstance()
                 * Result: The endpoints in HelloWorldEndpoint will be made available to clients.
                 */
                QRDetectionEndPoint.getInstance()
        ).build();
        /*
         * Attach the app's web server extension to the camera WebServer by opening the WebServerConnection.
         * Browser clients will not be able to interact with the camera app before this.
         */
        mWebServerConnection.open();
    }

    /**
     * Detaches the app's web server extension from the camera <i>WebServer</i>.
     */
    @SuppressLint("MissingPermission")
    private void detachWebServer() {
        if (mWebServerConnection != null) {
            /*
             * Detach the app's web server extension from the camera WebServer by closing the WebServerConnection.
             * Browser clients will not be able to interact with the camera app after this.
             */
            mWebServerConnection.close();
        }
    }

    /**
     * Gets the default videoCapture and starts the VideoSession.
     */
    @SuppressWarnings("MagicNumber")
    private void startVideoSession() {
        // Gets a default VideoCapture instance which does not scale, rotate, or modify the images received from the VideoPipeline.
        final VideoCapture capture = mVideoManager.getDefaultVideoCapture();
        Log.d(LOGTAG, String.format("getDefaultVideoCapture() with width %d and height %d", capture.getWidth(), capture.getHeight()));
        // Calculates the aspect ratio of the images coming from VideoPipeline (usually 16:9, but can vary per device).
        final double aspectRatio = (double)capture.getWidth() / capture.getHeight();
        // Initialize the requested width and height to what is specified in the VideoCapture.
        int requestWidth = capture.getWidth();
        int requestHeight = capture.getHeight();
        // Calculate new width and height to use, if the VideoCapture's height is greater than 1080 (Full HD resolution).
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
