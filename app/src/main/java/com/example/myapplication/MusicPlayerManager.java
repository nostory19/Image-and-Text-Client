package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.myapplication.model.Post;

public class MusicPlayerManager {
    private static final String TAG = "MusicPlayerManager";
    private static final String PREFS_NAME = "music_player_prefs";
    private static final String KEY_MUTE_STATE = "mute_state";

    private static MusicPlayerManager instance;
    private ExoPlayer player;
    private boolean isMuted = false;
    private String currentMusicUrl = "";
    private SharedPreferences prefs;

    private MusicPlayerManager(Context context){
        player = new ExoPlayer.Builder(context).build();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isMuted = prefs.getBoolean(KEY_MUTE_STATE, false);

        // 设置音频焦点监听
        setupAudioFocus(context);
    }

    public static synchronized MusicPlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicPlayerManager(context);
        }
        return instance;
    }

    private void setupAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    // 设置初始音量状态
                    updateVolume();
                }
            }
        });
    }

    public void playMusic(Post post) {
        if (post == null || post.getMusic() == null) {
            return;
        }

        String musicUrl = post.getMusic().getUrl();
        if (musicUrl == null || musicUrl.isEmpty()) {
            return;
        }

        // 如果是同一首音乐，不重新创建
        if (musicUrl.equals(currentMusicUrl) && player.isPlaying()) {
            return;
        }

        currentMusicUrl = musicUrl;

        try {
            MediaItem mediaItem = MediaItem.fromUri(musicUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);

            // 设置初始音量
            updateVolume();

            Log.d(TAG, "开始播放音乐: " + musicUrl);
        } catch (Exception e) {
            Log.e(TAG, "播放音乐失败: " + e.getMessage());
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        updateVolume();
        saveMuteState();
        Log.d(TAG, "静音状态: " + isMuted);
    }

    private void updateVolume() {
        float volume = isMuted ? 0.0f : 1.0f;
        player.setVolume(volume);
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            Log.d(TAG, "暂停播放");
        }
    }

    public void resume() {
        if (player != null && !player.isPlaying() && currentMusicUrl != null && !currentMusicUrl.isEmpty()) {
            player.setPlayWhenReady(true);
            Log.d(TAG, "恢复播放");
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
            player.clearMediaItems();
            currentMusicUrl = "";
            Log.d(TAG, "停止播放");
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
            instance = null;
            Log.d(TAG, "释放播放器");
        }
    }

    private void saveMuteState() {
        prefs.edit().putBoolean(KEY_MUTE_STATE, isMuted).apply();
    }

    public void resetMuteState() {
        isMuted = false;
        saveMuteState();
        updateVolume();
    }
}
