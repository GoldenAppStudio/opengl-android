package studio.goldenapp.imagefilters.programs;

import android.content.Context;

import studio.goldenapp.imagefilters.others.TextResourceReader;
import studio.goldenapp.imagefilters.helpers.ShaderHelper;

import static android.opengl.GLES20.glUseProgram;

public class ShaderProgram {
    // Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    // Shader program
    protected final int program;
    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        // Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResources(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResources(
                        context, fragmentShaderResourceId));
    }
    public void useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program);
    }
}

// yesterday i was facing an error the blanck screen error
// i have figueref it out as it was an error in corrdinates
// I have sorted it out the app to convert 
