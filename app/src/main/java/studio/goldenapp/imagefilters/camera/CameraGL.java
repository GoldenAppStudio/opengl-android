package studio.goldenapp.imagefilters.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.ButtonBarLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import studio.goldenapp.imagefilters.R;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static studio.goldenapp.imagefilters.others.Constants.BACK_CAMERA;
import static studio.goldenapp.imagefilters.others.Constants.CIRCLE;
import static studio.goldenapp.imagefilters.others.Constants.CUBE;
import static studio.goldenapp.imagefilters.others.Constants.FILTER_OFF;
import static studio.goldenapp.imagefilters.others.Constants.FLASH_LIGHT_OFF;
import static studio.goldenapp.imagefilters.others.Constants.FLASH_LIGHT_ON;
import static studio.goldenapp.imagefilters.others.Constants.FRONT_CAMERA;
import static studio.goldenapp.imagefilters.others.Constants.RECTANGLE;
import static studio.goldenapp.imagefilters.others.Constants.TRIANGLE;

public class CameraGL extends Activity implements SurfaceTexture.OnFrameAvailableListener {
    private Camera mCamera;
    private MyGLSurfaceView glSurfaceView;
    private SurfaceTexture surface;
    MyGL20Renderer renderer;

    ImageView switchCamera, captureImage, filterBtn, cancelFilters;
    ImageView settingBtn, flashBtn;
    FrameLayout frameLayout;

    int flashLightStatus, cameraDirection, filterToggle;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(String.valueOf(CameraGL.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)));

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Image-Filters", "failed to create directory: " + mediaStorageDir);
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            Log.d(TAG, "" + mediaFile);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_gl);

        initialiseUI();

        filterToggle = getIntent().getIntExtra("_filterToggle", 1);
        if (filterToggle == FILTER_OFF) {
            filterBtn.setImageResource(R.drawable.funnel);
        } else filterBtn.setImageResource(R.drawable.filter_on);

        glSurfaceView = new MyGLSurfaceView(this, filterToggle);
        renderer = glSurfaceView.getRenderer();
        frameLayout.addView(glSurfaceView);

        switchCamera.setOnClickListener(view -> switchCamera());
        settingBtn.setOnClickListener(view -> objectSelectDialog());
        filterBtn.setOnClickListener(view -> toggleFilter());
    }

    private void toggleFilter() {
        if (filterToggle == FILTER_OFF) {
            Intent intent = new Intent(CameraGL.this, CameraGL.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("_filterToggle", 0);
            startActivity(intent);
        } else {
            Intent intent = new Intent(CameraGL.this, CameraGL.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("_filterToggle", FILTER_OFF);
            startActivity(intent);
        }
    }

    private void objectSelectDialog() {
        Dialog dialog = new Dialog(CameraGL.this);
        dialog.setContentView(R.layout.objects_popup);
        dialog.setTitle("Choose a shape");
        TextView rectangle = dialog.findViewById(R.id.rectangle);
        TextView circle = dialog.findViewById(R.id.circle);
        TextView triangle = dialog.findViewById(R.id.triangle);
        TextView cube = dialog.findViewById(R.id.cube);

        rectangle.setOnClickListener(view -> renderShape(RECTANGLE));
        circle.setOnClickListener(view -> renderShape(CIRCLE));
        triangle.setOnClickListener(view -> renderShape(TRIANGLE));
        cube.setOnClickListener(view -> renderShape(CUBE));

        dialog.show();
    }

    private void renderShape(int shape) {}

    public void initialiseUI() {
        frameLayout = findViewById(R.id.texture);
        switchCamera = findViewById(R.id.btn_switch_camera);
        captureImage = findViewById(R.id.btn_record);
        filterBtn = findViewById(R.id.btn_filter);
        cancelFilters = findViewById(R.id.cancel_list);
        settingBtn = findViewById(R.id.btn_settings);
        flashBtn = findViewById(R.id.btn_flash);
    }

    public void startCamera(int texture) {
        surface = new SurfaceTexture(texture);
        surface.setOnFrameAvailableListener(this);
        renderer.setSurface(surface);

        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            mCamera.startFaceDetection();
            mCamera.enableShutterSound(true);
        } catch (IOException ioe) {
            Log.w("CameraGL", "CAM LAUNCH FAILED");
        }
    }

    private void switchFlashLight() {
        if (flashLightStatus == FLASH_LIGHT_OFF) {
            flashLightStatus = FLASH_LIGHT_ON;
        } else {
            flashLightStatus = FLASH_LIGHT_OFF;
        }
    }

    private void switchCamera() {
        /*if (cameraDirection == FRONT_CAMERA) cameraDirection = BACK_CAMERA;
        else cameraDirection = FRONT_CAMERA;*/

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(90);
        //requestLayout();

        mCamera.setParameters(parameters);
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
        mCamera.release();
        System.exit(0);
    }

}

/**
 *      in the weekend i was working with kotlin to freshen up the syntax and
 *      and all the stuff and was doing some opengl workaround with kotlin
 *      and today i will look forward to start the actual work
 *
 **/

