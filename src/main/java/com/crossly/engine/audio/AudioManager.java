package com.crossly.engine.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class AudioManager {

	private final long device;
	private final long context;

	public AudioManager() {
		device = ALC11.alcOpenDevice((ByteBuffer) null);
		if (device == 0L)
			throw new RuntimeException("alcOpenDevice failed!");
		context = ALC11.alcCreateContext(device, (IntBuffer) null);
		ALC11.alcMakeContextCurrent(context);
		ALCCapabilities capabilities = ALC.createCapabilities(device);
		AL.createCapabilities(capabilities);
	}

	public void cleanup() {
		ALC11.alcDestroyContext(context);
		ALC11.alcCloseDevice(device);
	}
}
