package com.maul.audacious;

import android.graphics.Bitmap;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    ImageButton m_play, m_prev, m_next;
    TextView m_text;
    SeekBar m_seek, m_volume;
    static Boolean m_connected = false;
    String m_filePath;
    byte[] m_imageRaw;
    Bitmap m_imageBitmap;
    ImageView m_imageView;

    static String username, password, hostname;
    static int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connect();
        if (m_connected == false) {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        }

        m_play = findViewById(R.id.button_play);
        m_play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    AudaciousCore.sendCommand("audtool playback-playpause");
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        m_next = findViewById(R.id.button_next);
        m_next.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    AudaciousCore.sendCommand("audtool playlist-advance");
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        m_prev = findViewById(R.id.button_previous);
        m_prev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    AudaciousCore.sendCommand("audtool playlist-reverse");
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        m_text = findViewById(R.id.textView);
        m_seek = findViewById(R.id.seekBar);

        m_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    String a = "audtool playback-seek " + Integer.toString(seekBar.getProgress());
                    AudaciousCore.sendCommand(a);
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        m_volume = findViewById(R.id.volume);

        m_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    String a = "audtool set-volume " + Integer.toString(seekBar.getProgress());
                    AudaciousCore.sendCommand(a);
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        m_imageView = findViewById(R.id.imageView);

        Runnable myRunnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                    update();
                }
            }
        };
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    public void update() {
        if (!m_connected) return;
        try {
            String tmp;

            tmp = AudaciousCore.sendCommand("audtool get-volume");
            m_volume.setProgress(Integer.parseInt(tmp.substring(0, tmp.length() - 1)));

            m_text.setText(AudaciousCore.sendCommand("audtool current-song"));
            tmp = AudaciousCore.sendCommand("audtool current-song-length-seconds");
            m_seek.setMax(Integer.parseInt(tmp.substring(0, tmp.length() - 1)));
            tmp = AudaciousCore.sendCommand("audtool current-song-output-length-seconds");
            m_seek.setProgress(Integer.parseInt(tmp.substring(0, tmp.length() - 1)));
            tmp = AudaciousCore.sendCommand("audtool current-song-filename");



            if (m_filePath != tmp) {
                m_filePath = tmp;
                m_imageRaw = AudaciousCore.getImage(tmp.substring(0, tmp.length() - 1));
                Bitmap imageBit;
                if (m_imageRaw != null)
                    imageBit = BitmapFactory.decodeByteArray(m_imageRaw, 0, m_imageRaw.length);
                else
                    imageBit = BitmapFactory.decodeResource(getResources(), R.drawable.no_cover);
                m_imageView.setImageBitmap(imageBit);
            }

            if(AudaciousCore.sendCommand("audtool playback-status").equals("playing\n"))
                m_play.setImageResource(android.R.drawable.ic_media_pause);
            else
                m_play.setImageResource(android.R.drawable.ic_media_play);

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
        return;
    }

    private void connect() {
        try {

            SharedPreferences prefs = getSharedPreferences("connection", MODE_PRIVATE);
            AudaciousCore.connect(prefs.getString("username",""), prefs.getString("password",""), prefs.getString("hostname",""), prefs.getInt("port", 0));

            m_connected = true;
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
            m_connected = false;
            return;
        }
        return;
    }
}