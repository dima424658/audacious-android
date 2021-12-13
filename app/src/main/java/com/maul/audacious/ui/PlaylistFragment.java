package com.maul.audacious.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maul.audacious.AudaciousCore;
import com.maul.audacious.OnSongClickListener;
import com.maul.audacious.PlaylistViewAdapter;
import com.maul.audacious.R;
import com.maul.audacious.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {
    public List<Song> songList;
    public static boolean init = false;
    private RecyclerView mRecyclerView;
    public static PlaylistViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        songList = new ArrayList<>();

        for(int i = 0; i < AudaciousCore.getPlaylistLength(); i++)
        {
            Song song = new Song();
            song.setName(AudaciousCore.getPlaylistSong(i));
            song.setLength(AudaciousCore.getPlaylistSongLength(i));
            song.setId(i);

            songList.add(song);
        }

        //customViewHolder.name.setText(AudaciousCore.getPlaylistSong(i));
        //customViewHolder.length.setText(AudaciousCore.getPlaylistSongLength(i));

        ///

        adapter = new PlaylistViewAdapter(view.getContext(), songList);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnSongClickListener() {
            @Override
            public void onSongClick(Song item) {
                AudaciousCore.playlistJump(item.getId());
            }

        });

        init = true;

    }
}


