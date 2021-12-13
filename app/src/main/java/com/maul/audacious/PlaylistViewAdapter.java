package com.maul.audacious;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Nilanchala Panigrahy on 10/25/16.
 */

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewAdapter.CustomViewHolder> {
    private List<Song> songItemList;
    private Context mContext;
    private OnSongClickListener onSongClickListener;
    private Color selColor;

    public PlaylistViewAdapter(Context context, List<Song> songList) {
        this.songItemList = songList;
        this.mContext = context;
        selColor = Color.valueOf(mContext.getResources().getColor(R.color.colorPrimary));
        selColor.alpha(100);
    }

    public void updateSongList(List<Song> songList) {
        this.songItemList = songList;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final Song song = songItemList.get(i);

        //Setting text view title
        customViewHolder.name.setText(song.getName());
        customViewHolder.length.setText(song.getLength());

        if(song.getPlay())
            customViewHolder.card.setCardBackgroundColor(selColor.toArgb());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSongClickListener.onSongClick(song);
            }
        };
        customViewHolder.name.setOnClickListener(listener);
        customViewHolder.length.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != songItemList ? songItemList.size() : 0);
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView length;
        protected CardView card;

        public CustomViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.song_name);
            this.length = view.findViewById(R.id.song_length);
            this.card = view.findViewById(R.id.card_view);
        }
    }


    public OnSongClickListener onSongClickListener() {
        return onSongClickListener;
    }

    public void setOnItemClickListener(OnSongClickListener onItemClickListener) {
        this.onSongClickListener = onItemClickListener;
    }
}