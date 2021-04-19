package studio.goldenapp.imagefilters;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

public class AirHockeyActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean renderSet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportES2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if(supportES2) {
            // Request an OpenGl ES2 compatible context
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign the renderer
            glSurfaceView.setRenderer(new AirHockeyRenderer(AirHockeyActivity.this));
            renderSet = true;
        } else {
            // Device does not support OpenGL ES
            Toast.makeText(this, "Device does not support OpenGL ES", Toast.LENGTH_SHORT).show();
        }

        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(renderSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(renderSet) {
            glSurfaceView.onResume();
        }
    }
}