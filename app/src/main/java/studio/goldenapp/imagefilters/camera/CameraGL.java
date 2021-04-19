package studio.goldenapp.imagefilters.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import studio.goldenapp.imagefilters.R;

import static android.content.ContentValues.TAG;
import static studio.goldenapp.imagefilters.others.Constants.FILTER_OFF;
import static studio.goldenapp.imagefilters.others.Constants.FLASH_LIGHT_OFF;
import static studio.goldenapp.imagefilters.others.Constants.FLASH_LIGHT_ON;
import static studio.goldenapp.imagefilters.others.Constants.NO_SHAPE;
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

    int flashLightStatus, cameraDirection, filterToggle, shape;

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

        filterToggle = getIntent().getIntExtra("_filterToggle", FILTER_OFF);
        if (filterToggle == FILTER_OFF) {
            filterBtn.setImageResource(R.drawable.funnel);
        } else filterBtn.setImageResource(R.drawable.filter_on);

        shape = getIntent().getIntExtra("_shape", NO_SHAPE);
        if (shape == NO_SHAPE) {
            glSurfaceView = new MyGLSurfaceView(this, filterToggle, NO_SHAPE);
        } else if (shape == TRIANGLE) {
            glSurfaceView = new MyGLSurfaceView(this, filterToggle, TRIANGLE);
        } else if (shape == RECTANGLE) {
            glSurfaceView = new MyGLSurfaceView(this, filterToggle, RECTANGLE);
        } else {
            glSurfaceView = new MyGLSurfaceView(this, filterToggle, NO_SHAPE);
        }

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
            intent.putExtra("_shape", shape);
            startActivity(intent);
        } else {
            Intent intent = new Intent(CameraGL.this, CameraGL.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("_filterToggle", FILTER_OFF);
            intent.putExtra("_shape", shape);
            startActivity(intent);
        }
    }

    private void objectSelectDialog() {
        Dialog dialog = new Dialog(CameraGL.this);
        dialog.setContentView(R.layout.objects_popup);
        dialog.setTitle("Choose a shape");
        TextView rectangle = dialog.findViewById(R.id.rectangle);
        TextView triangle = dialog.findViewById(R.id.triangle);
        TextView noShape = dialog.findViewById(R.id.no_shape);

        rectangle.setOnClickListener(view -> renderShape(RECTANGLE));
        triangle.setOnClickListener(view -> renderShape(TRIANGLE));
        noShape.setOnClickListener(view -> renderShape(NO_SHAPE));

        dialog.show();
    }

    private void renderShape(int shape) {
        Intent intent = new Intent(CameraGL.this, CameraGL.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (shape == TRIANGLE) {
            intent.putExtra("_shape", 1);
            intent.putExtra("_filterToggle", filterToggle);
        } else if (shape == RECTANGLE) {
            intent.putExtra("_shape", 2);
            intent.putExtra("_filterToggle", filterToggle);
        } else {
            intent.putExtra("_shape", 0);
            intent.putExtra("_filterToggle", filterToggle);
        }

        startActivity(intent);
    }

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
        /*if (flashLightStatus == FLASH_LIGHT_OFF) {
            flashLightStatus = FLASH_LIGHT_ON;
        } else {
            flashLightStatus = FLASH_LIGHT_OFF;
        }*/

        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void switchCamera() {
        /*if (cameraDirection == FRONT_CAMERA) cameraDirection = BACK_CAMERA;
        else cameraDirection = FRONT_CAMERA;*/

        /*Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(90);
        //requestLayout();

        mCamera.setParameters(parameters);*/

        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
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
