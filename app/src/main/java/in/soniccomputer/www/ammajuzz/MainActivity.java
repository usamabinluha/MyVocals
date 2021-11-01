package in.soniccomputer.www.ammajuzz;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import in.soniccomputer.www.ammajuzz.AudioService.MusicBinder;

import static in.soniccomputer.www.ammajuzz.AudioService.AS_MSG;
import static in.soniccomputer.www.ammajuzz.AudioService.AS_RESULT;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private AudioService audioService;
    private Intent playIntent;
    private boolean musicBound = false;

    BroadcastReceiver receiver;
    private String TAG;

    ImageButton nextBtn, prevBtn, playPauseBtn, rptBtn, stopBtn;
    TextView songTitle, songTotalDurationLabel, songCurrentDurationLabel;
    RelativeLayout rel;

    private SeekBar songProgressBar;
    private in.soniccomputer.www.ammajuzz.Utilities utils;
    private Handler mHandler = new Handler();

    private ConnectivityManager conManager;

    private String[] planets;
    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);

        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        playPauseBtn = (ImageButton) findViewById(R.id.button1);
        nextBtn = (ImageButton) findViewById(R.id.nextBtn);
        prevBtn = (ImageButton) findViewById(R.id.prevBtn);
        stopBtn = (ImageButton) findViewById(R.id.stopBtn);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        mainListView = (ListView) findViewById(R.id.mainListView);
        songTitle = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        rptBtn = (ImageButton) findViewById(R.id.rptBtn);
        utils = new Utilities();
        rptBtn.setImageResource(R.drawable.repeat_button);
        rel = (RelativeLayout) findViewById(R.id.rel);

        songProgressBar.setOnSeekBarChangeListener(MainActivity.this);

        planets = getResources().getStringArray(R.array.surahs);

        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll(Arrays.asList(planets));
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);
        mainListView.setAdapter(listAdapter);

        conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //AudioService
        Intent intent = new Intent(MainActivity.this, AudioService.class);
        intent.setAction(Constants.ACTION.INIT_ACTION);
        startService(intent);

        playIntent = new Intent(MainActivity.this, AudioService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(playIntent);
        //Toast.makeText(getApplicationContext(), "on Bound service", Toast.LENGTH_SHORT).show();

        //click events
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int k, long l) {
                rel.setVisibility(View.VISIBLE);
                audioService.releasePlayer();
                audioService.nullifyPlayer();
                audioService.setCurrentI(k);
                audioService.instantiatePlayer();
                audioService.startPlayer();
                audioService.showNotification();
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                playIntent.setAction(Constants.ACTION.PLAY_ACTION);
                startService(playIntent);
            }

        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playIntent.setAction(Constants.ACTION.NEXT_ACTION);
                startService(playIntent);
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playIntent.setAction(Constants.ACTION.PREV_ACTION);
                startService(playIntent);
            }
        });

        rptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioService.getIsRepeat()) {
                    audioService.setIsRepeat(true);
                    rptBtn.setImageResource(R.drawable.ic_repeat_one_pressed);
                    Toast.makeText(getApplicationContext(), "REPEAT is ON", Toast.LENGTH_SHORT).show();
                } else {
                    audioService.setIsRepeat(false);
                    rptBtn.setImageResource(R.drawable.repeat_button);
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProgressBar();
                if (!audioService.isPlayerNull()) {
                    stopPause();
                }
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TAG = intent.getStringExtra("TAG");

                if (TAG.equals("startPlayer")) {
                    playPauseBtn.setImageResource(R.drawable.pause_button);
                    updateProgressBar();
                    songTitle.setText(intent.getStringExtra(AS_MSG));
                    rel.setVisibility(View.VISIBLE);
                }

                if (TAG.equals("PAUSE")) {
                    playPauseBtn.setImageResource(R.drawable.play_button);
                }

                if (TAG.equals("NEXT | PREV")) {
                    play();
                    rel.setVisibility(View.VISIBLE);
                    songTitle.setText(intent.getStringExtra(AS_MSG));
                }

                if (TAG.equals("activityRecreated")) {
                    songProgressBar.setProgress(0);
                    songProgressBar.setMax(100);
                    updateProgressBar();
                    if (audioService.getIsRepeat()) {
                        rptBtn.setImageResource(R.drawable.ic_repeat_one_pressed);
                    }
                    playPauseBtn.setImageResource(R.drawable.pause_button);
                    rel.setVisibility(View.VISIBLE);
                    songTitle.setText(intent.getStringExtra(AS_MSG));
                }

                if (TAG.equals("finishActivity")) {
                    finish();
                    //Toast.makeText(getApplicationContext(), "send result worked", Toast.LENGTH_SHORT).show();
                }

                if (TAG.equals("SHOW")) {
                    rel.setVisibility(View.VISIBLE);
                }

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more_surahs:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Usama bin luha")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/search?q=pub:Usama bin luha")));
                }
                return true;

            case R.id.donate:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle("Be a part of sadhaqathul jaariah!")
                        .setCancelable(true)
                        .setMessage("If you would like to support financially or contribute(to build and deliver more apps), \nyou can whatsapp +917448348834" +
                                "\nor mail to castersimple@gmail.com")
                        .setPositiveButton("Jazakallahu khaira", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                dialog.cancel();
                            }
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(AS_RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void play() {
        if (audioService.isPlayerPlaying()) {
            playPauseBtn.setImageResource(R.drawable.pause_button);
        } else {
            playPauseBtn.setImageResource(R.drawable.play_button);
        }
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);
        updateProgressBar();
    }

    private void stopPause() {
        audioService.seekPlayerTo(0);
        audioService.pausePlayer();
        playPauseBtn.setImageResource(R.drawable.play_button);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            songCurrentDurationLabel.setText("" + utils.ourProgressToTimer(progress, audioService.getDuration()));

        }
    }


    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = audioService.getDuration();
            long currentDuration = audioService.getCurrentPosition();
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            songProgressBar.setProgress(progress);
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
        //updateProgressBar();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = audioService.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        audioService.seekPlayerTo(currentPosition);
        updateProgressBar();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            audioService = binder.getService();
            musicBound = true;
            //Toast.makeText(getApplicationContext(), "on Service Connected", Toast.LENGTH_SHORT).show();
            if (!audioService.isPlayerNull()) {
                if (!audioService.getIsPaused()) {
                    rel.setVisibility(View.VISIBLE);
                    long totalDuration = audioService.getDuration();
                    long currentDuration = audioService.getCurrentPosition();
                    songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
                    songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
                    int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
                    songProgressBar.setProgress(progress);
                    songTitle.setText(audioService.getCurrentTitle());
                    if (audioService.getIsRepeat()) {
                        rptBtn.setImageResource(R.drawable.ic_repeat_one_pressed);
                    }
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    public void onDestroy() {

        if (!audioService.isPlayerNull()) {
            if (!audioService.isPlayerPlaying()) {

                stopService(playIntent);

            }
        } else {
            stopService(playIntent);
        }

        unbindService(musicConnection);
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        //Toast.makeText(getApplicationContext(), "activity destroyed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = Activity.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}

