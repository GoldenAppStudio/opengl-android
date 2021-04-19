package studio.goldenapp.imagefilters.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;

class MyGLSurfaceView extends GLSurfaceView {
    MyGL20Renderer renderer;

    public MyGLSurfaceView(Context context, int isFilter, int shape) {
        super(context);

        setEGLContextClientVersion(2);

        renderer = new MyGL20Renderer(context, (CameraGL) context, isFilter, shape);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public MyGL20Renderer getRenderer() {
        return renderer;
    }
}
