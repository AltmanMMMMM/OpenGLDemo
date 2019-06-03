package com.lisi.opengldemo.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;

public class OpenGLUtil {
    private static final String TAG = "OPEN_GL";
    private static final int NO_TEXTURE = -1;

    /**
     * 加载着色器
     *
     * @param shaderSource shader代码
     * @param shaderType   shader类型 #GLES20.GL_VERTEX_SHADER #GLES20.GL_FRAGMENT_SHADER
     * @return shader句柄(索引)
     */
    private static int loadShader(final String shaderSource, int shaderType) {
        int[] compiled =  new int[1];
        // C function GLuint glCreateShader ( GLenum type )
        int shaderHandle = GLES20.glCreateShader(shaderType);
        // C function void glShaderSource ( GLuint shader, GLsizei count, const GLchar ** string, const GLint* length )
        GLES20.glShaderSource(shaderHandle, shaderSource);
        // C function void glCompileShader ( GLuint shader )
        GLES20.glCompileShader(shaderHandle);
        // C function void glGetShaderiv ( GLuint shader, GLenum pname, GLint *params )
        GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "compile shader error! error msg : " + GLES20.glGetShaderInfoLog(shaderHandle));
            return 0;
        }
        return shaderHandle;
    }

    /**
     * 加载并链接OpenGL程序对象
     *
     * @param vShaderSource GLES20.GL_VERTEX_SHADER
     * @param fShaderSource GLES20.GL_FRAGMENT_SHADER
     * @return
     */
    public static int loadProgram(final String vShaderSource, final String fShaderSource) {
        int vShaderHandle = loadShader(vShaderSource, GLES20.GL_VERTEX_SHADER);
        if (vShaderHandle == 0) {
            return 0;
        }
        int fShaderHandle = loadShader(fShaderSource, GLES20.GL_FRAGMENT_SHADER);
        if (fShaderHandle == 0) {
            return 0;
        }
        int[] link = new int[1];
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vShaderHandle);
        GLES20.glAttachShader(programHandle, fShaderHandle);
        GLES20.glLinkProgram(programHandle);
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] == 0) {
            Log.e(TAG, "link program error! error msg : " + GLES20.glGetProgramInfoLog(programHandle));
            GLES20.glDeleteProgram(programHandle);
            GLES20.glDeleteShader(vShaderHandle);
            GLES20.glDeleteShader(fShaderHandle);
            return 0;
        }
        GLES20.glDeleteShader(vShaderHandle);
        GLES20.glDeleteShader(fShaderHandle);
        return programHandle;
    }

    private static void setTextureParam(final int[] textures) {
        // 取得纹理句柄
        GLES20.glGenTextures(1, textures, 0);
        // 绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        // 设置纹理线性过滤 它会基于纹理坐标附近的纹理像素，计算出一个插值，近似出这些纹理像素之间的颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // 纹理环绕方式超出的部分会重复纹理坐标的边缘，产生一种边缘被拉伸的效果
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * 将bitmap加载成纹理
     *
     * @param bitmap 图片
     * @param textureId 已有的纹理id
     * @return 纹理id
     */
    public static int loadTexture(final Bitmap bitmap, final int textureId) {
        int[] textures = new int[1];
        if (textureId == NO_TEXTURE) {
            setTextureParam(textures);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            textures[0] = textureId;
        }
        return textures[0];
    }

    /**
     * 通过Buffer数据加载纹理
     *
     * @param buffer 图片数据
     * @param width 纹理宽度
     * @param height 纹理高度
     * @param textureId 已有的纹理id
     * @return 纹理id
     */
    public static int loadTexture(final ByteBuffer buffer, int width, int height, final int textureId) {
        int[] textures = new int[1];
        if (textureId == NO_TEXTURE) {
            setTextureParam(textures);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0,0 , width, height,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
            textures[0] = textureId;
        }
        return textures[0];
    }

}
