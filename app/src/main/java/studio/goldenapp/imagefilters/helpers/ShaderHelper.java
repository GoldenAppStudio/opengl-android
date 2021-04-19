package studio.goldenapp.imagefilters.helpers;

import android.opengl.GLES20;
import android.util.Log;

import studio.goldenapp.imagefilters.others.LoggerConfig;

import static android.opengl.GLES20.*;

public class ShaderHelper {
    public static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    public static int compileShader(int type, String shaderCode) {
        // create shader object and get object reference
        final int shaderObjectId = glCreateShader(type);

        // check if shader object creation failed
        if (shaderObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new shader");
            }
        }

        // upload shader source code into shader object
        glShaderSource(shaderObjectId, shaderCode);
        // compile the shader
        glCompileShader(shaderObjectId);

        // check if shader is compiled successfully
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        if (LoggerConfig.ON) {
            // Print the shader info log to the android logcat
            Log.v(TAG, "Result of compiling source: "
                    + "\n" + shaderCode + "\n:" +
                    glGetShaderInfoLog(shaderObjectId));
        }

        if (compileStatus[0] == 0) {
            // compiling the shader failed, delete the shader object
            glDeleteShader(shaderObjectId);

            if (LoggerConfig.ON) {
                Log.w(TAG, "Complication of shader has failed.");
            }

            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // create a shader program and get object reference
        final int programObjectId = glCreateProgram();

        // check if program object created successfully
        if (programObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new program");
            }

            return 0;
        }

        // attach shaders to shader program
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        // link the program
        glLinkProgram(programObjectId);

        // check if linking the program has failed
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        if (LoggerConfig.ON) {
            // Print the program info log to the android logcat
            Log.v(TAG, "Result of linking program: \n"
                    + glGetProgramInfoLog(programObjectId));
        }

        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Linking of program failed.");
            }
            return 0;
        }

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId) {
        // validate the program
       glValidateProgram(programObjectId);

       // check if validation of program is successful
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));
        return validateStatus[0] != 0;
    }

    public static int buildProgram(
            String vertexShaderSource, String fragmentShaderSource) {
        int program;
        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);
        if (LoggerConfig.ON) {
            validateProgram(program);
        }
        return program;
    }
}
