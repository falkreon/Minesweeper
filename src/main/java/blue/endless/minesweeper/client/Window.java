package blue.endless.minesweeper.client;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import blue.endless.minesweeper.Minesweeper;

public class Window {
	private long handle;
	private int width = 854;
	private int height = 480;
	private String title;
	
	public Window() {
		this("");
	}
	
	public Window(String title) {
		GLFW.glfwInit();
		GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint());
		
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_NATIVE_CONTEXT_API);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		
		this.title = title;
		this.handle = GLFW.glfwCreateWindow(this.width, this.height, title, 0L, 0L);
		if (this.handle == 0) throw new IllegalStateException("Could not initialize the OpenGL window");
		
		GLFW.glfwMakeContextCurrent(this.handle);
		GL.createCapabilities();
		
		GLFW.glfwSetFramebufferSizeCallback(this.handle, this::framebufferSizeCallback);
		GLFW.glfwSetWindowPosCallback(this.handle, this::windowPosCallback);
		GLFW.glfwSetWindowSizeCallback(this.handle, this::windowSizeCallback);
		GLFW.glfwSetWindowFocusCallback(this.handle, this::windowFocusCallback);
		GLFW.glfwSetCursorEnterCallback(this.handle, this::cursorEnterCallback);
		
		int[] stencilBits = {0};
		GL32.glGetFramebufferAttachmentParameteriv(GL32.GL_DRAW_BUFFER, GL32.GL_STENCIL, GL32.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE, stencilBits);
		Minesweeper.LOGGER.info("Stencil bits obtained: " + stencilBits[0]);
	}
	
	public long getHandle() {
		return handle;
	}
	
	public void setVisible(boolean visible) {
		if (visible) {
			GLFW.glfwShowWindow(handle);
		} else {
			GLFW.glfwHideWindow(handle);
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
		GLFW.glfwSetWindowTitle(handle, title);
	}
	
	public String getTitle() {
		return title;
	}
	
	private void framebufferSizeCallback(long handle, int width, int height) {
		
	}
	
	private void windowPosCallback(long handle, int x, int y) {
		
	}
	
	private void windowSizeCallback(long handle, int width, int height) {
		
	}
	
	private void windowFocusCallback(long handle, boolean focus) {
		
	}
	
	private void cursorEnterCallback(long handle, boolean entered) {
		
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(handle);
	}
	
	public void clear(float r, float g, float b, float a) {
		GL11.glClearColor(r, g, b, a);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
	}
	
	public void swapBuffers() {
		GLFW.glfwPollEvents();
		GLFW.glfwSwapBuffers(handle);
		
		//In case we load this method up with more stuff, better to be very responsive
		GLFW.glfwPollEvents();
	}
}
