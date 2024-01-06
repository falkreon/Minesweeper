package blue.endless.minesweeper.client;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

public record Painter(Window window, long hwin) {
	private static final String VERTEX_SHADER =
			"""
			#version 330 core
			layout (location = 0) in vec3 aPos;
			
			void main()
			{
				gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
			}
			""";
	
	private static final String FRAGMENT_SHADER =
			"""
			#version 330 core
			out vec4 FragColor;
			
			void main()
			{
				FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
			} 
			""";
	
	public Painter(Window window) {
		this(window, window.getHandle());
	}
	
	private static final int CLEAR_FLAGS =
			GL32.GL_COLOR_BUFFER_BIT |
			GL32.GL_STENCIL_BUFFER_BIT |
			GL32.GL_DEPTH_BUFFER_BIT;
	
	public void clear() {
		GL32.glClearColor(0, 0, 0, 0);
		GL32.glClear(CLEAR_FLAGS);
	}
	
	public void clear(RGBColor color) {
		GL32.glClearColor(color.gammaR(), color.gammaG(), color.gammaB(), color.alpha());
	}
	
	public void rect(float x, float y, float width, float height, RGBColor color) {
		float[] bufferData = {
			-0.5f, -0.5f, 0.0f,
			 0.5f, -0.5f, 0.0f,
			 0.0f,  0.5f, 0.0f
		};
		
		//TODO: Don't gen and delete vao+vbo each time we draw lol
		
		
		int vao = GL32.glGenVertexArrays();
		GL32.glBindVertexArray(vao);
		
		int buffer = GL32.glGenBuffers();
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, buffer);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
		
		GL32.glVertexAttribPointer(0, 3, GL32.GL_FLOAT, false, 3 * Float.BYTES, MemoryUtil.NULL);
		GL32.glEnableVertexAttribArray(0);
		
		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 3);
		
		GL32.glDeleteBuffers(buffer);
		GL32.glDeleteVertexArrays(vao);
	}
	
	public static ShaderProgram createShader() {
		return new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
	}
}
