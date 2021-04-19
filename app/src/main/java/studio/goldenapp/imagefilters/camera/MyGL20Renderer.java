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

import studio.goldenapp.imagefilters.camera.objects.Rectangle;
import studio.goldenapp.imagefilters.camera.objects.Triangle;

import static studio.goldenapp.imagefilters.others.Constants.NO_SHAPE;
import static studio.goldenapp.imagefilters.others.Constants.RECTANGLE;
import static studio.goldenapp.imagefilters.others.Constants.TRIANGLE;

public class MyGL20Renderer implements GLSurfaceView.Renderer {

    DirectVideo mDirectVideo;
    int texture;
    private SurfaceTexture surface;
    CameraGL delegate;
    Context context;
    int isFilter;
    Triangle triangle;
    Rectangle rectangle;
    int shape;

    public MyGL20Renderer(Context context, CameraGL _delegate, int isFilter, int shape) {
        delegate = _delegate;
        this.context = context;
        this.isFilter = isFilter;
        this.shape = shape;
    }

    public void onDrawFrame(GL10 unused) {
        float[] mtx = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        surface.setDefaultBufferSize(200, 300);

        surface.updateTexImage();
        surface.getTransformMatrix(mtx);

        mDirectVideo.draw();
       /* switch (shape) {
            case NO_SHAPE:
                return;
            case TRIANGLE:
                triangle.draw();
            case RECTANGLE:
                rectangle.draw();
        }*/

        if (shape == TRIANGLE) {
            triangle.draw();
        } else if (shape == RECTANGLE) {
            rectangle.draw();
        } else {
            return;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, javax.microedition.khronos.egl.EGLConfig eglConfig) {
        texture = createTexture();
        mDirectVideo = new DirectVideo(context, texture, isFilter);
        triangle = new Triangle();
        rectangle = new Rectangle();

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