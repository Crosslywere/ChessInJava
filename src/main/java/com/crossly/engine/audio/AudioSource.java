package com.crossly.engine.audio;

import com.crossly.engine.Engine;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.lwjgl.BufferUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.openal.AL11.*;

public class AudioSource {

	private final int buffer;
	private final int source;

	public enum Format {
		MP3, WAV, UNSPECIFIED
	}

	public AudioSource(String filepath, Format type) {
		buffer = alGenBuffers();
		source = alGenSources();
		filepath = Engine.getAbsolutePath(filepath);
		try {
			int format;
			ByteBuffer audioBuffer;
			format = switch (type) {
				case MP3 -> {
					audioBuffer = decodeMp3(filepath);
					yield AL_FORMAT_STEREO16;
				}
				case WAV -> {
					audioBuffer = decodeWav(filepath);
					yield AL_FORMAT_MONO16;
				}
				case UNSPECIFIED -> throw new UnsupportedOperationException("Unspecified audio format cannot be decoded");
			};
			alBufferData(buffer, format, audioBuffer, 44100);
			alSourcei(source, AL_BUFFER, buffer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public AudioSource(String filepath, Format type, float volume) {
		this(filepath, type);
		setVolume(volume);
	}

	public void delete() {
		alDeleteSources(source);
		alDeleteBuffers(buffer);
	}

	public void play() {
		alSourcePlay(source);
	}

	public float getElapsedTime() {
		return alGetSourcef(source, AL_SEC_OFFSET);
	}

	public void pause() {
		alSourcePause(source);
	}

	public void stop() {
		alSourceStop(source);
	}

	public float getVolume() {
		return alGetSourcef(source, AL_GAIN);
	}

	public void setVolume(float volume) {
		alSourcef(source, AL_GAIN, volume);
	}

	private ByteBuffer decodeMp3(String filepath) throws Exception {
		Bitstream bitstream = new Bitstream(new FileInputStream(filepath));
		Decoder decoder = new Decoder();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Header header = bitstream.readFrame();
		while (header != null) {
			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
			short[] pcm = output.getBuffer();
			for (var s : pcm) {
				stream.write(s & 0xFF);
				stream.write((s >> 8) & 0xFF);
			}
			bitstream.closeFrame();
			header = bitstream.readFrame();
		}
		byte[] data = stream.toByteArray();
		ByteBuffer result = BufferUtils.createByteBuffer(data.length);
		result.put(data);
		return result.flip();
	}

	private ByteBuffer decodeWav(String filepath) throws Exception {
		byte[] data = Files.readAllBytes(Paths.get(filepath));
		ByteBuffer result = BufferUtils.createByteBuffer(data.length);
		result.put(data);
		return result.flip();
	}
}
