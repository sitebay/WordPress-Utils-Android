package org.sitebay.android.util;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.util.Size;

import androidx.annotation.NonNull;

import com.daasuu.mp4compose.FillMode;
import com.daasuu.mp4compose.VideoFormatMimeType;
import com.daasuu.mp4compose.composer.ComposerInterface;
import com.daasuu.mp4compose.composer.ComposerProvider;
import com.daasuu.mp4compose.composer.ComposerUseCase.CompressVideo;
import com.daasuu.mp4compose.composer.Listener;
import com.daasuu.mp4compose.composer.Mp4ComposerBasic;

import org.m4m.AudioFormat;
import org.m4m.MediaComposer;
import org.m4m.MediaFileInfo;
import org.m4m.Uri;
import org.m4m.VideoFormat;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;

import java.io.IOException;


/**
 * This class implements functionality for simple video transcoding.
 * <p>
 * Input video is transcoded by using the H.264 Advanced Video Coding encoder.
 * Audio track is encoded with Advanced Audio Coding (AAC). Not resampled. Output sample rate and channel
 * count are the same as for input.
 */
public class WPVideoUtils {
    // Default parameters for the video encoder
    private static final String VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30; // 30fps
    private static final int IFRAME_INTERVAL = 2; // 2 seconds between I-frames

    // Default parameters for the audio encoder
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_OUTPUT_BIT_RATE = 96 * 1024;

    /**
     * This method return the media composer object that is in charge of video optimization.
     *
     * @param ctx The context
     * @param inputFile Input file path.
     * @param outFile Output file path.
     * @param listener The event listener
     * @return The media composer that is in charge of video transcoding, ready to be started,
     * or null in case the video cannot be transcoded.
     */
    public static MediaComposer getVideoOptimizationComposer(@NonNull Context ctx, @NonNull String inputFile,
                                                             @NonNull String outFile,
                                                             @NonNull org.m4m.IProgressListener listener,
                                                             int width, int bitrate) {
        AndroidMediaObjectFactory factory = new AndroidMediaObjectFactory(ctx);

        Uri m4mUri = new Uri(inputFile);
        MediaFileInfo mediaFileInfo = new MediaFileInfo(factory);
        try {
            mediaFileInfo.setUri(m4mUri);
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access the input file at " + inputFile, e);
            return null;
        }

        // Check the video resolution
        VideoFormat videoFormat = (VideoFormat) mediaFileInfo.getVideoFormat();
        if (videoFormat == null) {
            AppLog.w(AppLog.T.MEDIA, "Input file doesn't contain a video track?");
            return null;
        }
        if (videoFormat.getVideoFrameSize().width() < width) {
            AppLog.w(AppLog.T.MEDIA, "Input file width is lower than than " + width + ". Keeping the original file");
            return null;
        }
        if (videoFormat.getVideoFrameSize().height() == 0) {
            AppLog.w(AppLog.T.MEDIA, "Input file height is unknown. Can't calculate the correct "
                                     + "ratio for resizing. Keeping the original file");
            return null;
        }
        // Calculate the height keeping the correct aspect ratio
        float percentage = (float) width / videoFormat.getVideoFrameSize().width();
        float proportionateHeight = videoFormat.getVideoFrameSize().height() * percentage;
        int height = (int) Math.rint(proportionateHeight);

        AudioFormat audioFormat = (AudioFormat) mediaFileInfo.getAudioFormat();
        boolean isAudioAvailable = audioFormat != null;

        MediaComposer mediaComposer = new MediaComposer(factory, listener);
        try {
            mediaComposer.addSourceFile(inputFile);
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access the input file at " + inputFile, e);
            return null;
        }

        try {
            mediaComposer.setTargetFile(outFile, mediaFileInfo.getRotation());
        } catch (IOException e) {
            AppLog.e(AppLog.T.MEDIA, "Cannot access/write the output file at " + outFile, e);
            return null;
        }

        configureVideoEncoderWithDefaults(mediaComposer, width, height, bitrate);

        if (isAudioAvailable) {
            configureAudioEncoder(mediaComposer, audioFormat);
        }

        return mediaComposer;
    }

    // TODO: this should replace the equivalent function used for m4m lib once we fully introduce the Mp4Composer lib
    public static ComposerInterface getVideoOptimizationComposer(@NonNull String inputFile,
                                                                 @NonNull String outFile,
                                                                 @NonNull Listener listener,
                                                                 int width, int bitrate) {
        // NOTE: the parameters here (namely the AVC format type, IFrameInterval, the audio bitrate
        // and the CodecProfileLevel) have been selected based on what we had already as fixed parameters
        // in the original implementation that was using the media for mobile lib.
        // Two improvements could be:
        // - Investigate if the parameters set is already optimal and can be improved
        // - Expose them as parameters so that they can be eventually changed by some external logic
        ComposerInterface composer = ComposerProvider.INSTANCE.getComposerForUseCase(new CompressVideo(
                inputFile,
                outFile,
                VideoFormatMimeType.AVC,
                bitrate * 1024,
                2,
                96 * 1024,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                true
        ));

        Size srvVideoResolution = ((Mp4ComposerBasic) composer).getSrcVideoResolution();

        if (srvVideoResolution == null) {
            AppLog.w(AppLog.T.MEDIA, "Could not rescue source video resolution");
            return null;
        }

        if (srvVideoResolution.getWidth() < width) {
            AppLog.w(AppLog.T.MEDIA, "Input file width is lower than than " + width + ". Keeping the original file");
            return null;
        }
        if (srvVideoResolution.getHeight() == 0) {
            AppLog.w(AppLog.T.MEDIA, "Input file height is unknown. Can't calculate the correct "
                                     + "ratio for resizing. Keeping the original file");
            return null;
        }

        // Calculate the height keeping the correct aspect ratio
        float percentage = (float) width / srvVideoResolution.getWidth();
        float proportionateHeight = srvVideoResolution.getHeight() * percentage;
        int height = (int) Math.rint(proportionateHeight);

        composer.size(new Size(width, height))
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .listener(listener);

        return composer;
    }

    private static void configureVideoEncoderWithDefaults(MediaComposer mediaComposer, int width, int height,
                                                          int bitrate) {
        VideoFormatAndroid videoFormat = new VideoFormatAndroid(VIDEO_MIME_TYPE, width, height);
        videoFormat.setVideoBitRateInKBytes(bitrate);
        videoFormat.setVideoFrameRate(FRAME_RATE);
        videoFormat.setVideoIFrameInterval(IFRAME_INTERVAL);
        mediaComposer.setTargetVideoFormat(videoFormat);
    }

    private static void configureAudioEncoder(org.m4m.MediaComposer mediaComposer, AudioFormat audioFormat) {
        /**
         * TODO: Audio resampling is unsupported by current m4m release
         * Output sample rate and channel count are the same as for input.
         */
        AudioFormatAndroid aFormat = new AudioFormatAndroid(AUDIO_MIME_TYPE, audioFormat.getAudioSampleRateInHz(),
                                                            audioFormat.getAudioChannelCount());

        aFormat.setAudioBitrateInBytes(AUDIO_OUTPUT_BIT_RATE);
        aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mediaComposer.setTargetAudioFormat(aFormat);
    }
}
