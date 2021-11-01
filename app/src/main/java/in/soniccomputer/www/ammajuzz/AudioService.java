package in.soniccomputer.www.ammajuzz;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.IOException;
import java.util.Objects;


public class AudioService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    ZipResourceFile expansionFile = null;
    AssetFileDescriptor fd;

    private boolean isRepeat = false;
    static MediaPlayer player = null;
    private final IBinder musicBind = new MusicBinder();
    private static int currentI = 0;
    private static boolean isPaused = true;


    NotificationCompat.Builder builder;
    AudioManager mAudioManager;

    String[] planets;


    LocalBroadcastManager broadCaster;
    static final public String AS_RESULT = "in.soniccomputer.www.ammajuzz.AudioService.AS_RESULT";
    static final public String AS_MSG = "in.soniccomputer.www.ammajuzz.AudioService.AS_MSG";


    @Override
    public IBinder onBind(Intent intent) {

        //Toast.makeText(getApplicationContext(), "service on bind", Toast.LENGTH_SHORT).show();

        return musicBind;
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(getApplicationContext(), "service destroyed", Toast.LENGTH_SHORT).show();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Toast.makeText(getApplicationContext(), "service on unbind", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        planets = getResources().getStringArray(R.array.surahs);
        try {
            expansionFile = APKExpansionSupport.getAPKExpansionZipFile(getApplicationContext(), 1, 0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Toast.makeText(getApplicationContext(), "service created", Toast.LENGTH_SHORT).show();


        builder = new NotificationCompat.Builder(this);

        broadCaster = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (isRepeat) {
            instantiatePlayer();
            startPlayer();
        } else {
            next();
        }
        showNotification();
    }

    public class MusicBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//if (intent.getAction().equals(Constants.ACTION.INIT_ACTION)) {
        if (Constants.ACTION.INIT_ACTION.equals(intent.getAction())) {

            isPaused = true;

            if (player != null) {
                if (player.isPlaying()) {
                    sendResult(planets[currentI], "activityRecreated");
                    showNotification();
                    isPaused = false;
                }
            }

        } else if (Constants.ACTION.STARTFOREGROUND_ACTION.equals(intent.getAction())) {
            //Toast.makeText(getApplicationContext(), "onstart command", Toast.LENGTH_SHORT).show();

            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }


        } else if (Constants.ACTION.PREV_ACTION.equals(intent.getAction())) {
            if (!isPlayerNull()) {
                if (getCurrentPosition() > 3000) {
                    seekPlayerTo(0);
                } else {
                    if (isPlayerPlaying()) {
                        prev();
                    } else {
                        prev();
                        pausePlayer();
                    }
                    showNotification();
                }
            }
        } else if (Constants.ACTION.PLAY_ACTION.equals(intent.getAction())) {
            if (player == null) {
                instantiatePlayer();
                startPlayer();
            } else if (player.isPlaying()) {
                pausePlayer();
            } else {
                startPlayer();
            }
            showNotification();

        } else if (Constants.ACTION.NEXT_ACTION.equals(intent.getAction())) {
            if (!isPlayerNull()) {
                if (isPlayerPlaying()) {
                    next();
                } else {
                    next();
                    pausePlayer();
                }
                showNotification();
            }

        } else if (Constants.ACTION.STOPFOREGROUND_ACTION.equals(intent.getAction())) {
            pausePlayer();
            //Toast.makeText(getApplicationContext(), "intent received for service", Toast.LENGTH_SHORT).show();
            sendResult(planets[currentI], "finishActivity");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pausePlayer();
                break;
        }
    }

    public void startPlayer() {
        player.start();
        player.setOnCompletionListener(AudioService.this);
        sendResult(planets[currentI], "startPlayer");
        isPaused = false;
    }

    public void next() {
        releasePlayer();
        nullifyPlayer();
        if (currentI < planets.length - 1) {
            currentI++;
            instantiatePlayer();
            startPlayer();
        } else {
            currentI = 0;
            instantiatePlayer();
            startPlayer();
        }
        sendResult(planets[currentI], "NEXT | PREV");
    }

    public void prev() {
        releasePlayer();
        nullifyPlayer();
        if (currentI >= 1) {
            currentI--;
            instantiatePlayer();
            startPlayer();
        } else {
            currentI = planets.length - 1;
            instantiatePlayer();
            startPlayer();
        }
        sendResult(planets[currentI], "NEXT | PREV");
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setCurrentI(int k) {
        currentI = k;
    }

    public boolean isPlayerPlaying() {
        return player.isPlaying();
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
        }

    }

    public boolean isPlayerNull() {
        return player == null;
    }

    public void nullifyPlayer() {
        if (player != null) {
            player = null;
        }
    }

    public void pausePlayer() {
        if (player != null) {
            player.pause();
            sendResult(planets[currentI], "PAUSE");
            showNotification();
            isPaused = true;
        }
    }

    public void instantiatePlayer() {
        try {
            int x = currentI + 1;
            fd = expansionFile.getAssetFileDescriptor(String.valueOf(x)+".mp3");
            player  = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //player.reset();
            player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            player.prepare();
             //Toast.makeText(getApplicationContext(),String.valueOf(x)+".mp3",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setIsRepeat(boolean z) {
        isRepeat = z;
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {

        return player.getCurrentPosition();
    }

    public String getCurrentTitle() {
        return planets[currentI];
    }

    public void seekPlayerTo(int y) {
        player.seekTo(y);
    }

    public boolean getIsRepeat() {

        return isRepeat;
    }

    public void sendResult(String message, String tag) {
        Intent intent = new Intent(AS_RESULT);
        intent.putExtra("TAG", tag);
        //Toast.makeText(getApplicationContext(), tag, Toast.LENGTH_SHORT).show();
        intent.putExtra(AS_MSG, message);
        broadCaster.sendBroadcast(intent);
    }

    public void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, AudioService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, AudioService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, AudioService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, AudioService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if (isPlayerPlaying()) {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.pause_button);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.pause_button);
        } else {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.play_button);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.play_button);
        }


        //views.setTextViewText(R.id.status_bar_track_name, "Mishary (offline)");
        //bigViews.setTextViewText(R.id.status_bar_track_name, "Mishary (offline)");

        views.setTextViewText(R.id.status_bar_artist_name, planets[currentI]);
        bigViews.setTextViewText(R.id.status_bar_artist_name, planets[currentI]);


        //views.setTextViewText(R.id.status_bar_album_name, "Mishary Rashidh Al Afasy");
        //bigViews.setTextViewText(R.id.status_bar_album_name, "Mishary Rashidh Al Afasy");


        if (isPlayerPlaying()) {
            builder.setCustomContentView(views)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCustomBigContentView(bigViews)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_play_notifi)
                    .setContentIntent(pendingIntent);
        } else {
            builder.setCustomContentView(views)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCustomBigContentView(bigViews)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_pause_notifi)
                    .setContentIntent(pendingIntent);
        }

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
    }

}