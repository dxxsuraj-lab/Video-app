package com.example.videogallerypro;

import android.net.Uri;

public class VideoItem {
    public final long id;
    public final Uri uri;
    public final String name;
    public final long duration;
    public final long size;

    public VideoItem(long id, Uri uri, String name, long duration, long size) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
    }

    public String key() { return String.valueOf(id); }

    public String durationText() {
        long seconds = duration / 1000;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    public String sizeText() {
        if (size < 1024L * 1024L) return (size / 1024L) + " KB";
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
