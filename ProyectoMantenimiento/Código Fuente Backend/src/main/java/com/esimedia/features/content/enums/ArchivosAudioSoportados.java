package com.esimedia.features.content.enums;

public class ArchivosAudioSoportados {
    private ArchivosAudioSoportados() {
    }
    public static final String MP3 = "audio/mpeg";
    public static final String WAV = "audio/wav";
    public static final String WAV_PCM = "audio/vnd.wave";
    public static final String WAV_X = "audio/x-wav";
    public static final String OGG = "audio/ogg";
    public static final String FLAC = "audio/flac";
    public static final String AAC = "audio/aac";
    private static final String[] SUPPORTED_MIME_TYPES = {
        MP3, WAV, OGG, FLAC, AAC, WAV_PCM, WAV_X
    };

    public static boolean contains(String mimeType) {
        for (String type : SUPPORTED_MIME_TYPES) {
            if (mimeType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
