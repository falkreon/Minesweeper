package blue.endless.minesweeper.client;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

public class Painter {
	
	private static final String VERTEX_SHADER =
			"""
			#version 330 core
			uniform mat4 Matrix;
			
			layout (location = 0) in vec3 Pos;
			out vec4 FragPosition;
			
			void main()
			{
				gl_Position = FragPosition = Matrix * vec4(Pos.x, Pos.y, Pos.z, 1.0);
			}
			""";
	
	private static final String FRAGMENT_SHADER =
			"""
			#version 330 core
			uniform vec4 ShaderColor;
			in vec4 FragPosition;
			out vec4 FragColor;
			
			void main()
			{
				FragColor = ShaderColor;
			} 
			""";
	
	private static final int CLEAR_FLAGS =
			GL32.GL_COLOR_BUFFER_BIT |
			GL32.GL_STENCIL_BUFFER_BIT |
			GL32.GL_DEPTH_BUFFER_BIT;
	
	private final Window window;
	private final long hwin;
	private final ShaderProgram shader;
	private final int vbo;
	private final int vao;
	
	public Painter(Window window) {
		this.window = window;
		this.hwin = window.getHandle();
		
		this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		this.vbo = GL32.glGenBuffers();
		this.vao = GL32.glGenVertexArrays();
	}
	
	void startDrawing() {
		shader.use();
		GL32.glEnable(GL32.GL_BLEND);
		GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
		//TODO: Setup some important uniforms
	}
	
	public ShaderProgram getShader() {
		return shader;
	}
	
	void color(RGBColor value) {
		shader.set("ShaderColor", value);
	}
	
	void color(float r, float g, float b, float a) {
		shader.set("ShaderColor", r, g, b, a);
	}
	
	public void clear() {
		GL32.glClearColor(0, 0, 0, 0);
		GL32.glClear(CLEAR_FLAGS);
	}
	
	public void clear(RGBColor color) {
		GL32.glClearColor(color.gammaR(), color.gammaG(), color.gammaB(), color.alpha());
	}
	
	public void rect(float x, float y, float width, float height) {
		float[] bufferData = {
			x, y,
			x, y + height,
			x + width, y + height,
			x, y,
			x + width, y + height,
			x + width, y
		};
		
		GL32.glBindVertexArray(vao);
		
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
		
		GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 2 * Float.BYTES, MemoryUtil.NULL);
		GL32.glEnableVertexAttribArray(0);
		
		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
		
		
	}
	
	public void destroy() {
		shader.delete();
		GL32.glDeleteBuffers(vbo);
		GL32.glDeleteVertexArrays(vao);
	}

	
}
