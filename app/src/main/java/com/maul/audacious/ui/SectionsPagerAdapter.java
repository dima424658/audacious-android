package com.maul.audacious.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public static PlayerFragment m_player = new PlayerFragment();
    public static PlaylistFragment m_playlist = new PlaylistFragment();


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return m_player;
            case 1:
                return m_playlist;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}