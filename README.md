# Chess In Java

Attempting to create a 3D engine with chess as the example game

## Attributions

### Audio

- [stab-f-01-brvhrtz-224599.mp3](https://pixabay.com/sound-effects/stab-f-01-brvhrtz-224599/) - from pixabay.com
- [wall.jpg](https://learnopengl.com) - from learnopengl.com

# How to use

Currently, the only runnable class is the TestingApplication class located [here](src/test/java/com/crossly/TestingApplication.java)

It simply renders a textured square which can be clicked on to play a sound.

It also implements a fly camera ie `W`,`A`,`S`,`D` to move & hold the `Right Mouse Button` to rotate the camera.

### Currently Focused On

- Abstracting the framebuffer class so there can be different types of framebuffers eg. Picking / Id framebuffer