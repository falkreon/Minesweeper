package com.playsawdust.glow.gl;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.glow.Scope;
import com.playsawdust.glow.image.ImageData;
import com.playsawdust.glow.image.SrgbImageData;
import com.playsawdust.glow.image.color.RGBColor;
import com.playsawdust.glow.render.Painter;
import com.playsawdust.glow.vecmath.Matrix4;

public class WindowPainter implements Painter {

	private static final String VERTEX_SHADER =
			"""
			#version 330 core
			
			uniform mat4 Matrix;
			
			layout (location = 0) in vec2 Pos;
			layout (location = 1) in vec2 UV;
			
			out vec4 FragPosition;
			out vec2 FragUV;
			
			void main()
			{
				gl_Position = FragPosition = Matrix * vec4(Pos.x, Pos.y, 0.0, 1.0);
				FragUV = UV;
			}
			""";
	
	private static final String FRAGMENT_SHADER =
			"""
			#version 330 core
			
			uniform vec4 Color;
			uniform sampler2D Texture;
			
			in vec4 FragPosition;
			in vec2 FragUV;
			
			out vec4 FragColor;
			
			void main()
			{
				FragColor = Color * texture(Texture, FragUV);
				//FragColor = Color * vec4(FragUV, 1, 1);
			} 
			""";
	
	
	private Window window;
	private final ShaderProgram shader;
	private final int vbo;
	private final int vao;
	private Matrix4 matrix = Matrix4.IDENTITY;
	private Texture blankTexture;
	
	WindowPainter(Window window) {
		this.window = window;
		this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		this.vbo = GL32.glGenBuffers();
		this.vao = GL32.glGenVertexArrays();
		
		SrgbImageData data = new SrgbImageData(1,1);
		data.setPixel(0, 0, 0xFF_FFFFFF);
		blankTexture = new Texture(data);
	}
	
	void startDrawing() {
		shader.use();
		
		// Enable translucency
		GL32.glEnable(GL32.GL_BLEND);
		GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
		
		// Apply ortho transform
		matrix = ortho(0, window.getWidth(), window.getHeight(), 0, -1, 1);
		shader.set("Matrix", matrix);
		
		// Enable texturing and set params
		shader.set("Texture", 0);
		GL32.glActiveTexture(GL32.GL_TEXTURE0);
		GL32.glEnable(GL32.GL_TEXTURE_2D);
		GL32.glBindTexture(GL32.GL_TEXTURE_2D, blankTexture.getHandle());
		GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST_MIPMAP_NEAREST);
		GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
		GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_REPEAT);	
		GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_REPEAT);
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
		try (Scope scope = Scope.create()) {
			Texture texture = (image instanceof Texture tex) ? tex : scope.add(new Texture(image));
			
			final float px_x = 1.0f / image.getWidth();
			final float px_y = 1.0f / image.getHeight();
			
			final float u1 = srcX * px_x;
			final float v1 = srcY * px_y;
			
			final float u2 = u1 + (width * px_x);
			final float v2 = v1 + (height * px_y);
			
			float[] bufferData = {
				destX,         destY,          u1, v1,
				destX,         destY + height, u1, v2,
				destX + width, destY + height, u2, v2,
				destX,         destY,          u1, v1,
				destX + width, destY + height, u2, v2,
				destX + width, destY,          u2, v1
			};
			
			GL32.glBindVertexArray(vao);
			
			GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
			GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
			
			GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 0);
			GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
			GL32.glEnableVertexAttribArray(0);
			GL32.glEnableVertexAttribArray(1);
			
			setColor(RGBColor.fromGamma(1, 1, 1, opacity));
			GL32.glBindTexture(GL32.GL_TEXTURE_2D, texture.getHandle());
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			
			GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
		}
	}
	
	public void drawImage(ImageData image, int destX, int destY, int destWidth, int destHeight, int srcX, int srcY, int srcWidth, int srcHeight, float opacity) {
		try (Scope scope = Scope.create()) {
			Texture texture = (image instanceof Texture tex) ? tex : scope.add(new Texture(image));
			
			final float px_x = 1.0f / image.getWidth();
			final float px_y = 1.0f / image.getHeight();
			
			final float u1 = srcX * px_x;
			final float v1 = srcY * px_y;
			
			final float u2 = u1 + (srcWidth * px_x);
			final float v2 = v1 + (srcHeight * px_y);
			
			float[] bufferData = {
				destX,             destY,              u1, v1,
				destX,             destY + destHeight, u1, v2,
				destX + destWidth, destY + destHeight, u2, v2,
				destX,             destY,              u1, v1,
				destX + destWidth, destY + destHeight, u2, v2,
				destX + destWidth, destY,              u2, v1
			};
			
			GL32.glBindVertexArray(vao);
			
			GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
			GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
			
			GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 0);
			GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
			GL32.glEnableVertexAttribArray(0);
			GL32.glEnableVertexAttribArray(1);
			
			setColor(RGBColor.fromGamma(1, 1, 1, opacity));
			GL32.glBindTexture(GL32.GL_TEXTURE_2D, texture.getHandle());
			GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
			
			GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
		}
	}
	
	@Override
	public void drawTintImage(ImageData image, int destX, int destY, int srcX, int srcY, int width, int height, RGBColor color) {
		//if (image instanceof Texture texture) {
		//	GL32.glBindTexture(GL32.GL_TEXTURE_2D, texture.getHandle());
		//	shader.set("Texture", 0);
			
			
		//} else if (image instanceof LinearImageData linear) {
		//	
		//} else {
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
		//}
	}
	
	private void setColor(RGBColor color) {
		shader.set("Color", color);
	}
	
	@Override
	public void drawPixel(int x, int y, RGBColor color) {
		setColor(color);
		
		float[] bufferData = {
			x,     y,      0, 0,
			x,     y + 1,  0, 1,
			x + 1, y + 1,  1, 1,
			x,     y,      0, 0,
			x + 1, y + 1,  1, 1,
			x + 1, y,      1, 0
		};
		
		GL32.glBindVertexArray(vao);
		
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
		
		GL32.glEnableVertexAttribArray(0);
		GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, MemoryUtil.NULL);
		GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, MemoryUtil.NULL);
		
		
		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
	}

	@Override
	public void fillRect(int x, int y, int width, int height, RGBColor color) {
		setColor(color);
		
		float[] bufferData = {
				x,         y,           0, 0,
				x,         y + height,  0, 1,
				x + width, y + height,  1, 1,
				x,         y,           0, 0,
				x + width, y + height,  1, 1,
				x + width, y,           1, 0
			};
		
		GL32.glBindVertexArray(vao);
		
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, bufferData, GL32.GL_STATIC_DRAW);
		
		GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 0);
		GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		GL32.glEnableVertexAttribArray(0);
		
		GL32.glBindTexture(GL32.GL_TEXTURE_2D, blankTexture.getHandle());
		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, 6);
	}
	
	
	private Matrix4 ortho(double left, double right, double bottom, double top, double near, double far) {
		double tx = - (right+left) / (right-left);
		double ty = - (top+bottom) / (top-bottom);
		double tz = - (far+near) / (far-near);
		
		return new Matrix4(
			2.0/(right-left),                0,              0, tx,
			0,                2.0/(top-bottom),              0, ty,
			0,                               0,-2.0/(far-near), tz,
			0,                               0,              0,  1
		);
	}
}
