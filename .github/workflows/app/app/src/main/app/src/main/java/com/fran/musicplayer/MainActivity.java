package com.fran.musicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.media.MediaPlayer;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private final ArrayList<Uri> songs = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private MediaPlayer player;
    private int current = -1;
    private LinearLayout list;
    private TextView status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(8, 18, 40));
        getWindow().setNavigationBarColor(Color.rgb(8, 18, 40));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 20);
        root.setBackgroundColor(Color.rgb(8, 18, 40));

        TextView title = new TextView(this);
        title.setText("FRAN MUSIC PLAYER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 15, 0, 10);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("TU MÚSICA. TU ESTILO.");
        subtitle.setTextColor(Color.rgb(60, 170, 255));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 25);
        root.addView(subtitle);

        Button add = new Button(this);
        add.setText("＋ AÑADIR CANCIONES");
        root.addView(add);
        add.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 100);
        });

        status = new TextView(this);
        status.setText("Elige canciones para empezar");
        status.setTextColor(Color.LTGRAY);
        status.setPadding(0, 18, 0, 12);
        root.addView(status);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.MATCH_PARENT, 0, 1));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        Button previous = new Button(this);
        previous.setText("⏮");
        Button play = new Button(this);
        play.setText("▶ / ❚❚");
        Button next = new Button(this);
        next.setText("⏭");
        controls.addView(previous);
        controls.addView(play);
        controls.addView(next);
        root.addView(controls);

        previous.setOnClickListener(v -> playSong(current - 1));
        next.setOnClickListener(v -> playSong(current + 1));
        play.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                    status.setText("En pausa: " + names.get(current));
                } else {
                    player.start();
                    status.setText("Reproduciendo: " + names.get(current));
                }
            } else if (!songs.isEmpty()) {
                playSong(0);
            }
        });

        setContentView(root);
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
        getContentResolver().takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String name = uri.getLastPathSegment();
        if (name == null) name = "Canción " + songs.size();
        names.add(name);
        Button item = new Button(this);
        item.setText("♫  " + name);
        list.addView(item);
        item.setOnClickListener(v -> playSong(songs.indexOf(uri)));
        status.setText(songs.size() + " canciones añadidas");
    }

    private void playSong(int index) {
        if (songs.isEmpty()) return;
        if (index < 0) index = songs.size() - 1;
        if (index >= songs.size()) index = 0;
        current = index;
        try {
            if (player != null) {
                player.release();
                player = null;
            }
            player = MediaPlayer.create(this, songs.get(current));
            if (player == null) {
                status.setText("No se puede reproducir este archivo");
                return;
            }
            player.setOnCompletionListener(mp -> playSong(current + 1));
            player.start();
            status.setText("Reproduciendo: " + names.get(current));
        } catch (Exception e) {
            status.setText("Error al reproducir la canción");
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
