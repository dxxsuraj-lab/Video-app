package com.example.videogallerypro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;

public class PlayerActivity extends Activity {
    private Uri videoUri;
    private String videoName;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        VideoView videoView = findViewById(R.id.videoView);
        TextView title = findViewById(R.id.playerTitle);
        Button share = findViewById(R.id.shareButton);
        Button edit = findViewById(R.id.editButton);

        videoName = getIntent().getStringExtra("name");
        String rawUri = getIntent().getStringExtra("uri");
        if (rawUri == null) { finish(); return; }
        videoUri = Uri.parse(rawUri);
        title.setText(videoName == null ? "Video" : videoName);

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> videoView.start());
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Unable to play this video format.", Toast.LENGTH_SHORT).show();
            return true;
        });

        share.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("video/*");
            send.putExtra(Intent.EXTRA_STREAM, videoUri);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, "Share video"));
        });

        edit.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_EDIT);
            i.setDataAndType(videoUri, "video/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try { startActivity(Intent.createChooser(i, "Choose a video editor")); }
            catch (Exception e) {
                Toast.makeText(this, "Install a video editor to trim or edit videos.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
