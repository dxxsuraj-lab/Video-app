package com.example.videogallerypro;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int REQUEST_MEDIA = 101;
    private GridView grid;
    private TextView status;
    private final List<VideoItem> allVideos = new ArrayList<>();
    private final List<VideoItem> shownVideos = new ArrayList<>();
    private final Set<String> favourites = new HashSet<>();
    private boolean showingFavourites = false;
    private VideoAdapter adapter;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grid = findViewById(R.id.videoGrid);
        status = findViewById(R.id.statusText);
        favourites.addAll(getPreferences(MODE_PRIVATE).getStringSet("favourites", new HashSet<>()));
        adapter = new VideoAdapter();
        grid.setAdapter(adapter);

        findViewById(R.id.allButton).setOnClickListener(v -> {
            showingFavourites = false; updateShown();
        });
        findViewById(R.id.favButton).setOnClickListener(v -> {
            showingFavourites = true; updateShown();
        });

        if (hasMediaPermission()) loadVideos();
        else requestMediaPermission();
    }

    private boolean hasMediaPermission() {
        if (Build.VERSION.SDK_INT >= 33)
            return checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMediaPermission() {
        if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_MEDIA);
        else
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_MEDIA);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_MEDIA) {
            if (hasMediaPermission()) loadVideos();
            else status.setText("Video permission denied. Allow video access in App settings to show your videos.");
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (grid != null && hasMediaPermission()) loadVideos();
    }

    private void loadVideos() {
        allVideos.clear();
        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        };
        String sort = MediaStore.Video.Media.DATE_ADDED + " DESC";
        try (Cursor cursor = getContentResolver().query(collection, projection, null, null, sort)) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    Uri uri = ContentUris.withAppendedId(collection, id);
                    allVideos.add(new VideoItem(id, uri, cursor.getString(nameCol),
                        cursor.getLong(durationCol), cursor.getLong(sizeCol)));
                }
            }
        } catch (Exception e) {
            status.setText("Could not read videos: " + e.getMessage());
        }
        updateShown();
    }

    private void updateShown() {
        shownVideos.clear();
        for (VideoItem item : allVideos) {
            if (!showingFavourites || favourites.contains(item.key())) shownVideos.add(item);
        }
        status.setText(showingFavourites ? "Favourites: " + shownVideos.size() :
            "Videos found: " + shownVideos.size());
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void toggleFavourite(VideoItem item) {
        if (favourites.contains(item.key())) favourites.remove(item.key());
        else favourites.add(item.key());
        getPreferences(MODE_PRIVATE).edit().putStringSet("favourites", new HashSet<>(favourites)).apply();
        updateShown();
    }

    private void showActions(VideoItem item) {
        String[] options = {"Play", "Trim / Edit", "Share", "Delete", "Cancel"};
        new AlertDialog.Builder(this).setTitle(item.name).setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: openPlayer(item); break;
                case 1: launchEditor(item); break;
                case 2: shareVideo(item); break;
                case 3: confirmDelete(item); break;
            }
        }).show();
    }

    private void openPlayer(VideoItem item) {
        Intent i = new Intent(this, PlayerActivity.class);
        i.putExtra("uri", item.uri.toString());
        i.putExtra("name", item.name);
        startActivity(i);
    }

    private void shareVideo(VideoItem item) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("video/*");
        send.putExtra(Intent.EXTRA_STREAM, item.uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try { startActivity(Intent.createChooser(send, "Share video")); }
        catch (Exception e) { toast("No app available to share this video."); }
    }

    private void launchEditor(VideoItem item) {
        Intent edit = new Intent(Intent.ACTION_EDIT);
        edit.setDataAndType(item.uri, "video/*");
        edit.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(edit, "Choose a video editor"));
        } catch (Exception e) {
            // Many phones have no editor registered for ACTION_EDIT. Offer an app picker instead.
            Intent view = new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(item.uri, "video/*");
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try { startActivity(Intent.createChooser(view, "Open video with")); }
            catch (Exception ex) { toast("No compatible video editor is installed. Install a video editor and try again."); }
        }
    }

    private void confirmDelete(VideoItem item) {
        new AlertDialog.Builder(this).setTitle("Delete video?")
            .setMessage("This will remove \"" + item.name + "\" from the phone. Continue?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete", (d, w) -> deleteVideo(item)).show();
    }

    private void deleteVideo(VideoItem item) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                android.app.PendingIntent request = MediaStore.createDeleteRequest(getContentResolver(),
                    java.util.Collections.singletonList(item.uri));
                startIntentSenderForResult(request.getIntentSender(), 202, null, 0, 0, 0);
            } else {
                int deleted = getContentResolver().delete(item.uri, null, null);
                if (deleted > 0) {
                    favourites.remove(item.key());
                    saveFavourites();
                    toast("Video deleted.");
                    loadVideos();
                } else toast("Could not delete this video.");
            }
        } catch (Exception e) {
            toast("Delete failed: " + e.getMessage());
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 202) {
            if (resultCode == RESULT_OK) {
                toast("Video deleted.");
                loadVideos();
            } else toast("Delete cancelled.");
        }
    }

    private void saveFavourites() {
        getPreferences(MODE_PRIVATE).edit().putStringSet("favourites", new HashSet<>(favourites)).apply();
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    private class VideoAdapter extends BaseAdapter {
        @Override public int getCount() { return shownVideos.size(); }
        @Override public Object getItem(int position) { return shownVideos.get(position); }
        @Override public long getItemId(int position) { return shownVideos.get(position).id; }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) row = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_video, parent, false);
            VideoItem item = shownVideos.get(position);
            ImageView thumb = row.findViewById(R.id.thumb);
            TextView name = row.findViewById(R.id.videoName);
            TextView info = row.findViewById(R.id.videoInfo);
            Button fav = row.findViewById(R.id.fav);
            Button more = row.findViewById(R.id.more);

            Bitmap bitmap = null;
            try {
                if (Build.VERSION.SDK_INT >= 29)
                    bitmap = getContentResolver().loadThumbnail(item.uri, new android.util.Size(360, 240), null);
                else
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), item.id,
                        MediaStore.Video.Thumbnails.MINI_KIND, null);
            } catch (Exception ignored) {}
            if (bitmap != null) thumb.setImageBitmap(bitmap);
            else thumb.setImageResource(android.R.drawable.ic_media_play);

            name.setText(item.name);
            info.setText(item.durationText() + "  •  " + item.sizeText());
            fav.setText(favourites.contains(item.key()) ? "♥ Saved" : "♡ Like");
            fav.setOnClickListener(v -> toggleFavourite(item));
            more.setOnClickListener(v -> showActions(item));
            row.setOnClickListener(v -> openPlayer(item));
            return row;
        }
    }
}
