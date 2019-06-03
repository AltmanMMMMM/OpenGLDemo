package com.lisi.opengldemo.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.lisi.opengldemo.utils.OpenGLUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLTriangleRender implements GLSurfaceView.Renderer {

    private static final String V_POSITION = "vPosition";
    private static final String V_COLOR = "vColor";

    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private static final float[] VERTICES = {
            0.5f,  0.5f, 0.0f, // 顶点
            -0.5f, -0.5f, 0.0f, // 左下角
            0.5f, -0.5f, 0.0f // 右下角
    };
    private static final int COORDS_PER_VERTEX = 3; // 每个顶点由3个值(x,y,z)组成

    private static final float[] TRIANGLE_COLOR = {1.0f, 1.0f, 0, 1.0f};

    //顶点个数
    private final int VERTEX_COUNT = VERTICES.length / COORDS_PER_VERTEX;
    private final int VERTEX_STRIDER = COORDS_PER_VERTEX * 4; // 顶点之间的偏移量,每个顶点四个字节

    private int mProgramHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private FloatBuffer mVertexBuffer;

    // 视图、投影矩阵
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

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

        mProgramHandle = OpenGLUtil.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (width + 0.0f) / height;
        // 给透视投影矩阵赋值
        Matrix.frustumM(mProjectMatrix, 0 ,-ratio, ratio, -1, 1, 3, 7);
        // 给视图矩阵赋值
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f,0, 0,
                0,0, 1.0f, 0);
        // 计算最终变化矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgramHandle);
        // 获取顶点着色器的vPosition句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, V_POSITION);
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, VERTEX_STRIDER, mVertexBuffer);
        // 获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, V_COLOR);
        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, TRIANGLE_COLOR, 0);
        // 绘制三角形X
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }
}
