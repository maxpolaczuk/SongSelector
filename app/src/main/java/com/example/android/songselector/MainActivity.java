package com.example.android.songselector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor prefsEditor;
    SeekBar seekBar;
    ImageButton playBtn;
    Button chooseBtn;
    Button stopBtn;

    Uri selectedSong;
    String mySong;
    int mProgress;
    Runnable runnable;
    Handler mainHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences("PREFS", MODE_PRIVATE);
        mySong = sharedPrefs.getString("song", "");
        if (!mySong.equals("")) {
            selectedSong = Uri.parse(mySong);
        }

        playBtn = findViewById(R.id.play_button);
        chooseBtn = findViewById(R.id.choose_song_button);
        stopBtn = findViewById(R.id.stop_button);
        seekBar = findViewById(R.id.seek_bar);


        mainHandler = new Handler();

        if (selectedSong != null) {
            player = MediaPlayer.create(MainActivity.this, selectedSong);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            seekBar.setMax(player.getDuration());
            player.seekTo(0);

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                    if (input) {
                        if (player != null) {
                            player.seekTo(progress);
                        }
                        mProgress = progress;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }

        else {
            chooseSong();
            if(selectedSong != null) {
                player = MediaPlayer.create(MainActivity.this, selectedSong);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                seekBar.setMax(player.getDuration());
                player.seekTo(0);

            }

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                        if(input){
                            if(player != null){
                                player.seekTo(progress);
                            }
                            mProgress = progress;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });



        }

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedSong == null){
               chooseSong();

            }

                else {
                    if(player == null){
                        if(selectedSong != null){
                            player = MediaPlayer.create(MainActivity.this, selectedSong);
                            seekBar.setMax(player.getDuration());

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Please select a song file to play.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    if(player != null){

                        seekBar.refreshDrawableState();

                        if(mProgress > (player.getDuration() - 30)){
                            player.seekTo(0);
                        }
                        else{
                            player.seekTo(mProgress);
                        }

                        if(player.isPlaying()){
                            player.pause();
                            setPlayButtonImage();
                        }

                        else {
                            //player.prepareAsync();
                            player.start();
                            playCycle();
                            setPlayButtonImage();
                        }

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                //Restarts song from beginning
                                player.seekTo(0);
                                seekBar.setProgress(0);
                                player.start();
                                playCycle();
                            }
                        });
                    }
                }
            }
        });

        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audiofile_chooser_intent = new Intent();
                audiofile_chooser_intent.setAction(Intent.ACTION_GET_CONTENT);
                audiofile_chooser_intent.setType("audio/*");
                startActivityForResult(audiofile_chooser_intent, 1);

                stopPlaying();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                stopPlaying();
                if(selectedSong != null){
                    player = MediaPlayer.create(MainActivity.this, selectedSong);
                    seekBar.setMax(player.getDuration());
                    seekBar.setProgress(0);
                    mProgress = 0;
                    setPlayButtonImage();
                }

            }
        });


    }

    //Changes the play button depending on whether the song is playing or not
    private void setPlayButtonImage(){
        if(player.isPlaying()){
            playBtn.setImageResource(R.drawable.pause_button);
        }

        else {
            playBtn.setImageResource(R.drawable.play_button);
        }
    }

    public void playCycle() {
        seekBar.setProgress(player.getCurrentPosition());

        if(player.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            mainHandler.postDelayed(runnable, 1000);
        }
    }

    private void stopPlaying(){
        if(player != null){
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
    }

    //Creates dialog box for user to select an audio file from internal storage
    private void chooseSong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog songDialog = builder.create();
        songDialog.setTitle(R.string.select_song);
        songDialog.setMessage(getString(R.string.choose_song));

        songDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_browse), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent audiofile_chooser_intent = new Intent();
                audiofile_chooser_intent.setAction(Intent.ACTION_GET_CONTENT);
                audiofile_chooser_intent.setType("audio/*");


                audiofile_chooser_intent.addCategory(Intent.CATEGORY_OPENABLE);


                startActivityForResult(audiofile_chooser_intent, 1);
            }
        });

        songDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });

        songDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if(requestCode == 1){

            if(resultCode == RESULT_OK){
                selectedSong = data.getData();

                if(selectedSong != null){
                    mySong = selectedSong.toString();

                    //Testing another way to receive and store the audio file path...
                    /*
                    mySong = selectedSong.getPath();
                    try {
                        fis = new FileInputStream(mySong);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    try {
                        mySongDir = fis.getFD();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    */
                }
                //Save the Song Uri in string format to Shared Preferences
                prefsEditor = sharedPrefs.edit();
                prefsEditor.putString("song", mySong);
                prefsEditor.apply();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    private void prepareSong() {
        try {
            player.setDataSource(new FileInputStream(new File(selectedSong.getPath())).getFD());
            player.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e){
            e.printStackTrace();
       } catch(IOException e) {
           e.printStackTrace();
        }
    }
    */

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null){
            player.pause();
            setPlayButtonImage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null){
            player.release();
        }
        mainHandler.removeCallbacks(runnable);
    }

}
