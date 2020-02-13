package com.maul.audacious;

import android.graphics.Bitmap;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import android.support.design.widget.Snackbar;
import android.graphics.BitmapFactory;

import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import  android.content.SharedPreferences;


public class MainActivity extends AppCompatActivity {

    private ImageButton m_play, m_prev, m_next;
    private TextView m_text;
    private SeekBar m_seek, m_volume;
    private String m_filePath = "";
    private ImageView m_imageView;
    private Bitmap m_imageBitmap;
    private boolean m_playing;


    private final Runnable f_setPlay = new Runnable() {
        public void run() {
            m_play.setImageResource(android.R.drawable.ic_media_pause);
        }
    };

    private final Runnable f_setPause = new Runnable() {
        public void run() {
            m_play.setImageResource(android.R.drawable.ic_media_play);
        }
    };

    private final Runnable f_setImage = new Runnable() {
        public void run() {
            m_imageView.setImageBitmap(m_imageBitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connect();

        m_play = findViewById(R.id.button_play);
        m_play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playPause();
            }
        });
        m_next = findViewById(R.id.button_next);
        m_next.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playNext();
            }
        });
        m_prev = findViewById(R.id.button_previous);
        m_prev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playPrevious();
            }
        });
        m_text = findViewById(R.id.textView);
        m_seek = findViewById(R.id.seekBar);

        m_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudaciousCore.setPlaybackSeek(seekBar.getProgress());
            }
        });

        m_volume = findViewById(R.id.volume);

        m_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudaciousCore.setVolume(seekBar.getProgress());
            }
        });

        m_imageView = findViewById(R.id.imageView);

        m_playing = AudaciousCore.isPlaying();

        Runnable myRunnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        update();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        };
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    private void update() {
        if (!AudaciousCore.isConnected())
            connect();
        else {
            m_volume.setProgress(AudaciousCore.getVolume());
            m_text.setText(AudaciousCore.getCurrentSong());
            m_seek.setMax(AudaciousCore.getCurrentSongLength());
            m_seek.setProgress(AudaciousCore.getCurrentSongOutputLength());

            String tmp = AudaciousCore.getCurrentSongFilename();

            if (tmp != null)
                if (!m_filePath.equals(tmp)) {
                    m_filePath = tmp;
                    byte[] imageRaw = AudaciousCore.getImageRaw(tmp.substring(0, tmp.length() - 1));
                    if (imageRaw != null)
                        m_imageBitmap = BitmapFactory.decodeByteArray(imageRaw, 0, imageRaw.length);
                    else
                        m_imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_cover);

                    runOnUiThread(f_setImage);
                }

            if (AudaciousCore.isPlaying()) {
                runOnUiThread(f_setPlay);
                m_playing = true;
            }

            if (!AudaciousCore.isPlaying() && m_playing) {
                runOnUiThread(f_setPause);
                m_playing = false;
            }
        }
    }

    private void connect() {
        SharedPreferences prefs = getSharedPreferences("connection", MODE_PRIVATE);
        AudaciousCore.connect(prefs.getString("username", ""), prefs.getString("password", ""), prefs.getString("hostname", ""), prefs.getInt("port", 0));

        if (!AudaciousCore.isConnected()) {
            String msg = "Failed to connect to " + prefs.getString("hostname", "") + ":" + prefs.getInt("port", 0);
            Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
            Log.w("MainActivity", msg);

            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        }
    }
}
