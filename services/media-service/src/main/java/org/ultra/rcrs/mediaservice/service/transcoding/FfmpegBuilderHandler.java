package org.ultra.rcrs.mediaservice.service.transcoding;

import org.ultra.rcrs.pipeline.Handler;

public class FfmpegBuilderHandler implements Handler<AudioProcessData, AudioProcessData> {

    @Override
    public AudioProcessData process(AudioProcessData input) {
        //-i pipe:0 -c:a libvorbis -b:a 320k -ar 44100 -vn -map_metadata -1 -y -f ogg pipe:1
        var builder = new ProcessBuilder("ffmpeg", String.format("-i pipe:0 -c:a %s -b:a %s -ar %s -vn -map_metadata -1 -y -f %s pipe:1",
                input.getCodec(), input.getBitrate(), input.getSampleRate(), input.getContainer()));
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        input.setProcessBuilder(builder);
        return input;
    }
}
