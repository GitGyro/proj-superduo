/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.jaschke.alexandria.barcode;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

import it.jaschke.alexandria.CameraPreview.CameraSourcePreview;
import it.jaschke.alexandria.CameraPreview.GraphicOverlay;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;

/**
 * Activity for the multi-tracker app.  This app detects faces and barcodes with the rear facing
 * camera, and draws overlay graphics to indicate the position, size, and ID of each face and
 * barcode.
 */
public final class MultiTrackerActivity extends AppCompatActivity {
    private static final String TAG = "MultiTracker";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.vision_scanner);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }



    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {


        Context context = getApplicationContext();


        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        //Plus::
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.EAN_13 | Barcode.EAN_8)
                .build();
        //Plus::
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay ,
                new GraphicTracker.Callback(){
                    @Override
                    public void onMatch(String barcodeValue){
                        //Log.e(TAG,"barcode = "+barcodeValue);
//                        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
//                        tg.startTone(ToneGenerator.TONE_PROP_ACK, 500);

                        ToneGenerator tg1 = new ToneGenerator(AudioManager.STREAM_ALARM,100);
                        tg1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,500);
//                       tg1.startTone(ToneGenerator.TONE_PROP_BEEP2,500);

                        Intent data = new Intent();
                        data.putExtra(MainActivity.BARCODE_KEY, barcodeValue);
                        setResult(RESULT_OK,data);
                        finish();


                    }
                });


        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        // A multi-detector groups the two detectors together as one detector.  All images received
        // by this detector from the camera will be sent to each of the underlying detectors, which
        // will each do face and barcode detection, respectively.  The detection results from each
        // are then sent to associated tracker instances which maintain per-item graphics on the
        // screen.
        MultiDetector multiDetector = new MultiDetector.Builder()
                .add(barcodeDetector)
                .build();

        if (!multiDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        mCameraSource = new CameraSource.Builder(getApplicationContext(), multiDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
    //Plus::
    /**
     * <p>Returns the supplied {@link com.google.android.gms.vision.CameraSource}'s {@link android.hardware.Camera} instance that is being used.</p>
     * <p>
     * If you want to set any of the camera parameters, here's an example that sets the focus mode to continuous auto focus and enables the flashlight:
     * <blockquote>
     * <code>
     * Camera.Parameters params = camera.getParameters();
     * params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
     * params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
     * camera.setParameters(params);
     * </code>
     * </blockquote>
     * Note that your app might have to hold the permission {@code android.permission.FLASHLIGHT}, so put this in your manifest:
     * <blockquote>
     * <code>
     * &lt;uses-permission android:name="android.permission.FLASHLIGHT" /&gt;
     * </code>
     * </blockquote>
     * </p>
     * <p>
     * Also note that the CameraSource's {@link CameraSource#start()} or
     * {@link CameraSource#start(SurfaceHolder)} has to be called and the camera image has to be
     * showing prior using this method as the CameraSource only creates the camera after calling
     * one of those methods and the camera is not available immediately. You could implement some
     * kind of a callback method for the SurfaceHolder that notifies you when the imaging is ready
     * or use a direct action (e.g. button press) to get the camera and change its parameters.
     * </p>
     * <p>
     * Check out <a href="https://github.com/googlesamples/android-vision/blob/master/face/multi-tracker/app/src/main/java/com/google/android/gms/samples/vision/face/multitracker/ui/camera/CameraSourcePreview.java#L84">CameraSourcePreview.java</a>
     * which contains the method <code>startIfReady()</code> that has the following line:
     * <blockquote><code>mCameraSource.start(mSurfaceView.getHolder());</code></blockquote><br>
     * After this call you can use our <code>cameraFocus(...)</code> method because the camera is ready.
     * </p>
     * <p>
     *     <b>DO NOT</b> set the following parameters using this instance as it might break the detectors:
     *     <ul>
     *         <li>picture size (<code>setPictureSize(...)</code>)</li>
     *         <li>preview size (<code>setPreviewSize(...)</code>)</li>
     *         <li>preview FPS range (<code>setPreviewFpsRange(...)</code>)</li>
     *         <li>preview format (<code>setPreviewFormat(...)</code>)</li>
     *         <li>rotation (<code>setRotation(...)</code>)</li>
     *     </ul>
     * </p>
     *
     * @param cameraSource The CameraSource built with {@link com.google.android.gms.vision.CameraSource.Builder}.
     * @return the actual {@link android.hardware.Camera} instance used by the supplied {@link com.google.android.gms.vision.CameraSource}, or {@code null} on failure.
     * @see android.hardware.Camera
     */
    public static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }

                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }

        return null;
    }
    public static boolean cameraAutoFocus(@NonNull CameraSource cameraSource, String autoFocus)
    {
        Camera camera = getCamera(cameraSource);
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();

            if (!params.getSupportedFocusModes().contains(autoFocus)) {
                return false;
            }

            params.setFocusMode(autoFocus);
            camera.setParameters(params);
            return true;
        }

        return false;

    }

}
