package com.crossly.chess;

import com.crossly.engine.graphics.Framebuffer;
import com.crossly.engine.graphics.Renderbuffer;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL33.*;

public class BoardFramebuffer extends Framebuffer {

	public record Data(int pieceId, int boardPosId) {
		public static int generateBoardPosId(int x, int y) {
			return (x << 4) + y;
		}
		public static int generateBoardPosId(Vector2f pos) {
			return generateBoardPosId((int) pos.x(), (int) pos.y());
		}
	}

	private final Renderbuffer pieceIdBuffer;
	private final Renderbuffer boardPosIdBuffer;
	private final Renderbuffer depthStencilBuffer;

	public BoardFramebuffer(int width, int height) {
		super(width, height);
		pieceIdBuffer = new Renderbuffer(width, height, GL_R32I);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_RENDERBUFFER, pieceIdBuffer.getRenderbufferId());
		boardPosIdBuffer = new Renderbuffer(width, height, GL_R32I);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_RENDERBUFFER, boardPosIdBuffer.getRenderbufferId());
		depthStencilBuffer = new Renderbuffer(width, height, GL_DEPTH24_STENCIL8);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer.getRenderbufferId());
		checkStatus();
		unbind();
	}

	@Override
	public void clearData() {
		super.bind();
		glClearBufferiv(GL_COLOR, 1, new int[] { -1 });
		glClearBufferiv(GL_COLOR, 2, new int[] { -1 });
	}

	@Override
	public void bind() {
		super.bind();
		glDrawBuffers(new int[] { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2 });
	}

	@Override
	public void delete() {
		super.delete();
		pieceIdBuffer.delete();
		boardPosIdBuffer.delete();
		depthStencilBuffer.delete();
	}

	public Data getIds(int x, int y) {
		if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
			return new Data(-1, -1);
		}
		super.bind();
		int[] pd = new int[1];
		glReadBuffer(GL_COLOR_ATTACHMENT1);
		glReadPixels(x, y, 1, 1, GL_RED_INTEGER, GL_INT, pd);
		int[] bd = new int[1];
		glReadBuffer(GL_COLOR_ATTACHMENT2);
		glReadPixels(x, y, 1, 1, GL_RED_INTEGER, GL_INT, bd);
		unbind();
		return new Data(pd[0], bd[0]);
	}
}
