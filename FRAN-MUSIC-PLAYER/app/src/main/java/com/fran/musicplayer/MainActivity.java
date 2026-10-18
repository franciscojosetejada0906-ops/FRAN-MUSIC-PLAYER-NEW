package com.fran.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private final ArrayList<Uri> songs = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private MediaPlayer player;
    private int current = -1;
    private TextView nowPlaying;
    private LinearLayout songList;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(7, 15, 35));
        getWindow().setNavigationBarColor(Color.rgb(7, 15, 35));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 24, 22, 18);
        root.setBackgroundColor(Color.rgb(7, 15, 35));

        TextView title = new TextView(this);
        title.setText("FRAN MUSIC PLAYER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 12, 0, 18);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        Button add = new Button(this);
        add.setText("AÑADIR CANCIONES");
        add.setOnClickListener(v -> chooseSongs());
        root.addView(add, new LinearLayout.LayoutParams(-1, -2));

        nowPlaying = new TextView(this);
        nowPlaying.setText("Elige canciones para empezar");
        nowPlaying.setTextColor(Color.LTGRAY);
        nowPlaying.setTextSize(16);
        nowPlaying.setGravity(Gravity.CENTER);
        nowPlaying.setPadding(0, 18, 0, 12);
        root.addView(nowPlaying, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        Button previous = new Button(this);
        previous.setText("⏮");
        previous.setOnClickListener(v -> playIndex(current - 1));
        playButton = new Button(this);
        playButton.setText("▶ / ❚❚");
        playButton.setOnClickListener(v -> togglePlayback());
        Button next = new Button(this);
        next.setText("⏭");
        next.setOnClickListener(v -> playIndex(current + 1));
        controls.addView(previous);
        controls.addView(playButton);
        controls.addView(next);
        root.addView(controls, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        songList = new LinearLayout(this);
        songList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(songList);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        scrollParams.topMargin = 12;
        root.addView(scroll, scrollParams);
        setContentView(root);
    }

    private void chooseSongs() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 100 || resultCode != RESULT_OK || data == null) return;
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                addSong(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            addSong(data.getData());
        }
    }

    private void addSong(Uri uri) {
        if (songs.contains(uri)) return;
        songs.add(uri);
        names.add(getName(uri));
        int index = songs.size() - 1;
        Button item = new Button(this);
        item.setText((index + 1) + ". " + names.get(index));
        item.setAllCaps(false);
        item.setOnClickListener(v -> playIndex(index));
        songList.addView(item, new LinearLayout.LayoutParams(-1, -2));
        if (songs.size() == 1) nowPlaying.setText("Lista preparada: " + songs.size() + " canción");
        else nowPlaying.setText("Lista preparada: " + songs.size() + " canciones");
    }

    private String getName(Uri uri) {
        String result = "Canción";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) result = cursor.getString(column);
            }
        } catch (Exception ignored) {}
        return result;
    }

    private void playIndex(int index) {
        if (songs.isEmpty()) {
            Toast.makeText(this, "Añade canciones primero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (index < 0) index = songs.size() - 1;
        if (index >= songs.size()) index = 0;
        current = index;
        releasePlayer();
        try {
            player = new MediaPlayer();
            player.setDataSource(this, songs.get(current));
            player.setOnCompletionListener(mp -> playIndex(current + 1));
            player.prepare();
            player.start();
            nowPlaying.setText("Reproduciendo:\n" + names.get(current));
            playButton.setText("❚❚ Pausar");
        } catch (IOException | RuntimeException e) {
            releasePlayer();
            Toast.makeText(this, "No se ha podido reproducir ese archivo", Toast.LENGTH_LONG).show();
        }
    }

    private void togglePlayback() {
        if (player == null) {
            if (!songs.isEmpty()) playIndex(current < 0 ? 0 : current);
            else Toast.makeText(this, "Añade canciones primero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (player.isPlaying()) {
            player.pause();
            playButton.setText("▶ Reanudar");
        } else {
            player.start();
            playButton.setText("❚❚ Pausar");
        }
    }

    private void releasePlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
