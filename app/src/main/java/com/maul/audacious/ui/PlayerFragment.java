package com.maul.audacious.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.maul.audacious.AudaciousCore;
import com.maul.audacious.PlaylistViewAdapter;
import com.maul.audacious.R;
import com.maul.audacious.Song;

import java.util.ArrayList;
import java.util.List;

public class PlayerFragment extends Fragment {
    public Thread t_update;
    private ImageButton m_play, m_prev, m_next;
    private TextView m_text;
    private SeekBar m_seek, m_volume;
    private String m_filePath = "";
    private ImageView m_imageView;
    private Bitmap m_imageBitmap;
    private boolean m_playing;
    private View snackBar;

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

    private final Runnable f_update = new Runnable() {
        @Override
        public void run() {
            m_volume.setProgress(AudaciousCore.getVolume());
            m_text.setText(AudaciousCore.getCurrentSong());
            m_seek.setMax(AudaciousCore.getCurrentSongLength());
            m_seek.setProgress(AudaciousCore.getCurrentSongOutputLength());
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = getActivity().getSharedPreferences("connection", Context.MODE_PRIVATE);
        AudaciousCore.connect(prefs.getString("username", ""), prefs.getString("password", ""), prefs.getString("hostname", ""), prefs.getInt("port", 0));

        if (!AudaciousCore.isConnected()) {
            String msg = "Failed to connect to " + prefs.getString("hostname", "") + ":" + prefs.getInt("port", 0);
            //Snackbar.make(view.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
            Log.w("MainActivity", msg);

            Intent intent = new Intent(getActivity().getApplicationContext(), ConnectActivity.class);
            startActivity(intent);
        }

        snackBar = view.findViewById(android.R.id.content);

        m_play = view.findViewById(R.id.button_play);
        m_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playPause();
            }
        });
        m_next = view.findViewById(R.id.button_next);
        m_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playNext();
            }
        });
        m_prev = view.findViewById(R.id.button_previous);
        m_prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AudaciousCore.playPrevious();
            }
        });
        m_text = view.findViewById(R.id.textView);
        m_seek = view.findViewById(R.id.seekBar);

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

        m_volume = view.findViewById(R.id.volume);

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

        m_imageView = view.findViewById(R.id.imageView);
        m_playing = AudaciousCore.isPlaying();

        t_update = new Thread(new Runnable() {
            public void run() {
                while (AudaciousCore.isConnected()) {
                    try {
                        Thread.sleep(500);
                        update();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Snackbar.make(snackBar, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }

            }
        });

        t_update.start();
    }

    private void update() {
        getActivity().runOnUiThread(f_update);

        String tmp = AudaciousCore.getCurrentSongFilename();

        if (tmp != null)
            if (!m_filePath.equals(tmp)) {
                m_filePath = tmp;
                byte[] imageRaw = AudaciousCore.getImageRaw(tmp);
                if (imageRaw != null)
                    m_imageBitmap = BitmapFactory.decodeByteArray(imageRaw, 0, imageRaw.length);
                else
                    m_imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_cover);

                getActivity().runOnUiThread(f_setImage);
            }

        if (AudaciousCore.isPlaying() && !m_playing) {
            getActivity().runOnUiThread(f_setPlay);
            m_playing = true;
        }

        if (!AudaciousCore.isPlaying() && m_playing) {
            getActivity().runOnUiThread(f_setPause);
            m_playing = false;
        }

        if (PlaylistFragment.init) {
            List<Song> list = new ArrayList<>();
            int current = AudaciousCore.getPlaylistPosition();
            for (int i = 0; i < AudaciousCore.getPlaylistLength(); i++) {
                Song song = new Song();
                song.setName(AudaciousCore.getPlaylistSong(i));
                song.setLength(AudaciousCore.getPlaylistSongLength(i));
                song.setId(i);
                if(i == current)
                    song.setPlay();

                list.add(song);
            }

            PlaylistFragment.adapter.updateSongList(list);

            getActivity().runOnUiThread(new Runnable(){
                public void run() {
                    PlaylistFragment.adapter.notifyDataSetChanged();
                }
            });
        }

    }
}