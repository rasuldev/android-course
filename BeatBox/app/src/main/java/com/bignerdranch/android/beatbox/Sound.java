package com.bignerdranch.android.beatbox;

/**
 * Created by Rasul on 06.05.2016.
 */
public class Sound {
    private String mAssetPath;
    private String mName;
    private Integer mSoundId;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".wav", "");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public int getSoundId() {
        return mSoundId;
    }

    public void setSoundId(int soundId) {
        mSoundId = soundId;
    }
}
