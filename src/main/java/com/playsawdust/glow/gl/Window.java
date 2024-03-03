package com.playsawdust.glow.gl;

import java.util.function.IntConsumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.glow.render.Painter;

import blue.endless.minesweeper.Minesweeper;
import blue.endless.tinyevents.Event;
import blue.endless.tinyevents.EventFactories;
import blue.endless.tinyevents.function.IntBiConsumer;

public class Window {
	private GLFWErrorCallback glfwErrorCallback;
	private GLDebugMessageCallbackI khrErrorCallback;
	private final long handle;
	//private final Painter painter;
	private final WindowPainter painter;
	private int width = 854;
	private int height = 480;
	private String title;
	
	private static Event<IntBiConsumer> onLeftMouseUp = EventFactories.intBiConsumer();
	private static Event<IntBiConsumer> onRightMouseUp = EventFactories.intBiConsumer();
	private static Event<IntConsumer> onKeyDown = EventFactories.intConsumer();
	private static Event<IntConsumer> onKeyUp = EventFactories.intConsumer();
	
	public Window() {
		this("");
	}
	
	public Window(String title) {
		GLFW.glfwInit();
		glfwErrorCallback = GLFWErrorCallback.createPrint();
		GLFW.glfwSetErrorCallback(glfwErrorCallback);
		
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
		
		installErrorCallback();
		
		GLFW.glfwSetFramebufferSizeCallback(this.handle, this::framebufferSizeCallback);
		GLFW.glfwSetWindowPosCallback(this.handle, this::windowPosCallback);
		GLFW.glfwSetWindowSizeCallback(this.handle, this::windowSizeCallback);
		GLFW.glfwSetWindowFocusCallback(this.handle, this::windowFocusCallback);
		GLFW.glfwSetCursorEnterCallback(this.handle, this::cursorEnterCallback);
		GLFW.glfwSetMouseButtonCallback(this.handle, this::mouseButtonCallback);
		GLFW.glfwSetKeyCallback(this.handle, this::keyCallback);
		
		//painter = new Painter(this);
		painter = new WindowPainter(this);
		
		int[] viewportDimensions = new int[4];
		GL32.glGetIntegerv(GL32.GL_VIEWPORT, viewportDimensions);
		this.width = viewportDimensions[2];
		this.height = viewportDimensions[3];
		
		
		Minesweeper.LOGGER.info(GL32.glGetString(GL32.GL_VERSION));
		Minesweeper.LOGGER.info("Stencil bits obtained: " + GL32.glGetInteger(GL32.GL_STENCIL_BITS));
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
		this.width = width;
		this.height = height;
		GL32.glViewport(0, 0, width, height);
	}
	
	//private ImageData createCompatibleImage(ImageData input) {
		
	//}
	
	private void windowPosCallback(long handle, int x, int y) {
		
	}
	
	private void windowSizeCallback(long handle, int width, int height) {
		
	}
	
	private void windowFocusCallback(long handle, boolean focus) {
		
	}
	
	private void cursorEnterCallback(long handle, boolean entered) {
		
	}
	
	private void mouseButtonCallback(long handle, int button, int action, int modifiers) {
		if (action != 0) return; //For now, we only care about mouseUp events
		
		double[] xa = new double[1];
		double[] ya = new double[1];
		GLFW.glfwGetCursorPos(handle, xa, ya);
		int x = (int) xa[0];
		int y = (int) ya[0];
		
		switch(button) {
			case 0 -> onLeftMouseUp.invoker().accept(x, y);
			case 1 -> onRightMouseUp.invoker().accept(x, y);
		}
	}
	
	private void keyCallback(long window, int key, int scancode, int action, int mods) {
		//1 is down, 0 is up, 2 is repeat
		if (action == GLFW.GLFW_PRESS) {
			onKeyDown.invoker().accept(scancode);
		} else if (action == GLFW.GLFW_RELEASE) {
			onKeyUp.invoker().accept(scancode);
		}
	}
	
	public Event<IntBiConsumer> onLeftMouseUp() {
		return onLeftMouseUp;
	}
	
	public Event<IntBiConsumer> onRightMouseUp() {
		return onRightMouseUp;
	}
	
	public Event<IntConsumer> onKeyDown() {
		return onKeyDown;
	}
	
	public Event<IntConsumer> onKeyUp() {
		return onKeyUp;
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(handle);
	}
	
	public WindowPainter startDrawing() {
		painter.startDrawing();
		return painter;
	}
	
	public Painter getPainter() {
		return this.painter;
	}
	
	public void clear(float r, float g, float b, float a) {
		GL32.glClearColor(r, g, b, a);
		GL32.glClear(GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_COLOR_BUFFER_BIT);
	}
	
	public void poll() {
		GLFW.glfwPollEvents();
	}
	
	public void swapBuffers() {
		GLFW.glfwPollEvents();
		GLFW.glfwSwapBuffers(handle);
		
		//In case we load this method up with more stuff, better to be very responsive
		GLFW.glfwPollEvents();
	}
	
	public void installErrorCallback() {
		if (GL.getCapabilities().GL_KHR_debug) {
			GL32.glEnable(KHRDebug.GL_DEBUG_OUTPUT);
			GL32.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
			
			khrErrorCallback = (sourceId, typeId, id, severity, length, messagePtr, userParam) -> {
				String message = MemoryUtil.memASCII(messagePtr);
				
				if (typeId == KHRDebug.GL_DEBUG_TYPE_ERROR) {
					Minesweeper.LOGGER.error(message);
				} else {
					Minesweeper.LOGGER.info(message);
				}
			};
			KHRDebug.glDebugMessageCallback(khrErrorCallback, MemoryUtil.NULL);
		}
	}
	
	public void delete() {
		GLFW.glfwDestroyWindow(handle);
		
	}

	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
}
