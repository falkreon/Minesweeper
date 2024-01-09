package blue.endless.minesweeper.client;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.color.Colors;
import com.playsawdust.glow.image.color.RGBColor;
import com.playsawdust.glow.render.Painter;
import com.playsawdust.glow.vecmath.Matrix4;

public class WindowPainter implements Painter {

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
			uniform vec4 Color;
			in vec4 FragPosition;
			out vec4 FragColor;
			
			void main()
			{
				FragColor = Color;
			} 
			""";
	
	
	private Window window;
	private final ShaderProgram shader;
	private final int vbo;
	private final int vao;
	private Matrix4 matrix = Matrix4.IDENTITY;
	
	WindowPainter(Window window) {
		this.window = window;
		this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		this.vbo = GL32.glGenBuffers();
		this.vao = GL32.glGenVertexArrays();
	}
	
	void startDrawing() {
		shader.use();
		GL32.glEnable(GL32.GL_BLEND);
		GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
		
		matrix = new Matrix4(
			2f/(window.getWidth() - 0), 0, 0, -1,
			0,-2f/(window.getHeight()-0), 0, 1,
			0, 0,-1, 0,
			0, 0, 0, 1
			);
		shader.set("Matrix", matrix);
		//TODO: Setup some important uniforms
	}

	@Override
	public int getWidth() {
		return window.getWidth();
	}
	
	@Override
	public int getHeight() {
		return window.getHeight();
	}

	@Override
	public void drawImage(ImageData image, int destX, int destY, int srcX, int srcY, int width, int height, float opacity) {
		//TODO: Optimize
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				drawPixel(destX + x, destY + y, image.getLinearPixel(srcX + x, srcY + y).withOpacity(opacity));
			}
		}
	}
	
	@Override
	public void drawTintImage(ImageData image, int destX, int destY, int srcX, int srcY, int width, int height, RGBColor color) {
		//TODO: Optimize
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				RGBColor srcColor = image.getLinearPixel(srcX + x, srcY + y);
				RGBColor appliedColor = new RGBColor(
						srcColor.alpha() * color.alpha(),
						srcColor.r() * color.r(),
						srcColor.g() * color.g(),
						srcColor.b() * color.b()
					);
				drawPixel(destX + x, destY + y, appliedColor);
			}
		}
	}
	
	private void setColor(RGBColor color) {
		shader.set("Color",
			(float) color.r(), /* Colors.linearElementToGamma(color.r()),*/
			(float) Colors.linearElementToGamma(color.g()),
			(float) Colors.linearElementToGamma(color.b()),
			color.alpha()
			);
	}
	
	@Override
	public void drawPixel(int x, int y, RGBColor color) {
		setColor(color);
		
		float[] bufferData = {
			x, y,
			x, y + 1,
			x + 1, y + 1,
			x, y,
			x + 1, y + 1,
			x + 1, y
		};
		
		GL32.glBindVertexArray(vao);
		
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
		
		GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 2 * Float.BYTES, MemoryUtil.NULL);
		GL32.glEnableVertexAttribArray(0);
		
		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
	}

	@Override
	public void fillRect(int x, int y, int width, int height, RGBColor color) {
		setColor(color);
		
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

}
