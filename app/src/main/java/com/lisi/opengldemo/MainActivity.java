package com.lisi.opengldemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lisi.opengldemo.render.TextureRender;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mSurfaceView;
    private TextureRender mRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_demo);

        mSurfaceView = findViewById(R.id.gl_surface_view);
        // 指定版本号
        mSurfaceView.setEGLContextClientVersion(2);
        mRender = new TextureRender();
        mRender.setBitmap(bitmap);
        mSurfaceView.setRenderer(mRender);
        // 设置渲染模式
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

}
