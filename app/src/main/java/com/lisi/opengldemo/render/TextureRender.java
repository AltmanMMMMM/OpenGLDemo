package com.lisi.opengldemo.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.lisi.opengldemo.utils.OpenGLUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureRender implements GLSurfaceView.Renderer {

  private static final String V_POSITION = "vPosition";
  private static final String V_MATRIX = "vMatrix";
  private static final String V_COORDINATE = "vCoordinate";
  private static final String V_TEXTURE = "vTexture";

  private static final String VERTEX_SHADER =
      "attribute vec4 vPosition;"
      + "attribute vec2 vCoordinate;"
      + "uniform mat4 vMatrix;"
      + "varying vec2 aCoordinate;"
      + "void main() {"
      + "  gl_Position = vMatrix * vPosition;"
      + "  aCoordinate = vCoordinate;"
      + "}";

  private static final String FRAGMENT_SHADER =
      "precision mediump float;"
      + "uniform sampler2D vTexture;"
      + "varying vec2 aCoordinate;"
      + "void main() {"
      + "  gl_FragColor=texture2D(vTexture, aCoordinate);"
      + "}";

  private static final float[] VERTICES = {
      -1.0f, 1.0f,  //左上角
      -1.0f, -1.0f, //左下角
      1.0f,  1.0f,  //右上角
      1.0f,  -1.0f  //右下角
  };

  private static final float[] COORDINATE = {
      0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
  };

  private int mProgramHandle;
  private int mPositionHandle;
  private int mMatrixHandle;
  private int mCoordinateHandle;
  private int mTextureHandle;
  private int mTextureId;
  private FloatBuffer mVertexBuffer;
  private FloatBuffer mCoorBuffer;

  // 视图、投影矩阵
  private float[] mViewMatrix = new float[16];
  private float[] mProjectMatrix = new float[16];
  private float[] mMVPMatrix = new float[16];

  private Bitmap mBitmap;

  public void setBitmap(Bitmap bitmap) {
      Log.e("xpp", "bitmap = " + bitmap);
      this.mBitmap = bitmap;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    // 申请native层内存空间
    ByteBuffer buffer = ByteBuffer.allocateDirect(VERTICES.length * 4);
    buffer.order(ByteOrder.nativeOrder());
    //将坐标数据转换为FloatBuffer，用以传入OpenGL ES程序
    mVertexBuffer = buffer.asFloatBuffer();
    mVertexBuffer.put(VERTICES);
    // 避免顺序出错，出现意想不到的结果
    mVertexBuffer.position(0);

    ByteBuffer cBuffer = ByteBuffer.allocateDirect(COORDINATE.length * 4);
    cBuffer.order(ByteOrder.nativeOrder());
    mCoorBuffer = cBuffer.asFloatBuffer();
    mCoorBuffer.put(COORDINATE);
    mCoorBuffer.position(0);

    GLES20.glEnable(GLES20.GL_TEXTURE_2D);
    mProgramHandle = OpenGLUtil.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);

    // 获取顶点着色器的vMatrix句柄
    mMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, V_MATRIX);

    // 获取顶点着色器的vPosition句柄
    mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, V_POSITION);

    // 获取纹理坐标的vCoordinate句柄
    mCoordinateHandle =
        GLES20.glGetAttribLocation(mProgramHandle, V_COORDINATE);

    // 获取纹理vTexture句柄
    mTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, V_TEXTURE);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    GLES20.glViewport(0, 0, width, height);

    int w = mBitmap.getWidth();
    int h = mBitmap.getHeight();
    float bmpRatio = w / (float)h;
    float portRatio = width / (float)height;
    if (width > height) {
      if (bmpRatio > portRatio) {
        Matrix.orthoM(mProjectMatrix, 0, -portRatio * bmpRatio,
                      portRatio * bmpRatio, -1, 1, 3, 5);
      } else {
        Matrix.orthoM(mProjectMatrix, 0, -portRatio / bmpRatio,
                      portRatio / bmpRatio, -1, 1, 3, 5);
      }
    } else {
      if (bmpRatio > portRatio) {
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / portRatio * bmpRatio,
                      1 / portRatio * bmpRatio, 3, 5);
      } else {
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -bmpRatio / portRatio,
                      bmpRatio / portRatio, 3, 5);
      }
    }
    //设置相机位置
    Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f,
            0f, 0f, 1.0f, 0.0f);
    //计算变换矩阵
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    GLES20.glClearColor(0, 0, 0, 0);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    GLES20.glUseProgram(mProgramHandle);

    GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
    // 启用三角形顶点的句柄
    GLES20.glEnableVertexAttribArray(mPositionHandle);
    // 启用纹理坐标
    GLES20.glEnableVertexAttribArray(mCoordinateHandle);
    GLES20.glUniform1i(mTextureHandle, 0);
    mTextureId = OpenGLUtil.loadTexture(mBitmap, -1);
    GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
    GLES20.glVertexAttribPointer(mCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mCoorBuffer);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    //禁止顶点数组的句柄
    GLES20.glDisableVertexAttribArray(mPositionHandle);
  }
}
