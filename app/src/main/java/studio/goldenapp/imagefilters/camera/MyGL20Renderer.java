package studio.goldenapp.imagefilters.camera;

import android.content.Context;
import android.content.SyncContext;
import android.graphics.SurfaceTexture;
import android.opengl.EGLConfig;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.SurfaceView;

import javax.microedition.khronos.opengles.GL10;

import studio.goldenapp.imagefilters.camera.objects.Triangle;

public class MyGL20Renderer implements GLSurfaceView.Renderer {

    DirectVideo mDirectVideo;
    int texture;
    private SurfaceTexture surface;
    CameraGL delegate;
    Context context;
    int isFilter;
    Triangle triangle;

    public MyGL20Renderer(Context context, CameraGL _delegate, int isFilter) {
        delegate = _delegate;
        this.context = context;
        this.isFilter = isFilter;
    }

    public void onDrawFrame(GL10 unused) {
        float[] mtx = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        surface.setDefaultBufferSize(200, 300);

        surface.updateTexImage();
        surface.getTransformMatrix(mtx);

        mDirectVideo.draw();
        triangle.draw();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        texture = createTexture();
        mDirectVideo = new DirectVideo(context, texture, isFilter);
        triangle = new Triangle();

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        delegate.startCamera(texture);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    static public int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    static private int createTexture() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        return texture[0];
    }

    public void setSurface(SurfaceTexture _surface) {
        surface = _surface;
    }
}