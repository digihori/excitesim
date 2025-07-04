package com.github.digihori.excitesim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int[] mBtnResIds = {
            R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6
    };
    public static int[] digitImages = {
            R.drawable.d0, R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4,
            R.drawable.d5, R.drawable.d6, R.drawable.d7, R.drawable.d8, R.drawable.d9
    };
    public static int[] tvIds = {
            R.id.text1, R.id.text2, R.id.text3, R.id.text4,
            R.id.text5, R.id.text6, R.id.text7, R.id.text8
    };
    private int mode;
    private final Random random = new Random();
    private SoundPool soundPool;
    private int soundNormal, soundReach, soundFanfare, soundFever;
    private AnimationDrawable anime1, anime2;
    private final Handler mHandler0 = new Handler();
    private final Handler mHandler1 = new Handler();
    private final Handler mHandler2 = new Handler();
    private Runnable stop1, stop2, auto;
    public static class ResultNum {
        int d1;
        int d2;
    }
    private ResultNum digit;
    private ImageView imgView1, imgView2;
    private SwitchCompat autoSW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int id : mBtnResIds) {
            findViewById(id).setOnClickListener(this);
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(2)
                .build();

        // サウンドのロード
        soundNormal = soundPool.load(this, R.raw.nomal, 1);
        soundReach = soundPool.load(this, R.raw.reach, 1);
        soundFanfare = soundPool.load(this, R.raw.fanfare, 1);
        soundFever = soundPool.load(this, R.raw.fever, 1);

        // load が終わったか確認する??
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            Log.w("debug","sampleId="+sampleId);
            Log.w("debug","status="+status);
        });

        imgView1 = findViewById(R.id.img1);
        imgView2 = findViewById(R.id.img3);
        imgView1.setBackgroundResource(R.drawable.digit_list);
        imgView2.setBackgroundResource(R.drawable.digit_list);
        anime1 = (AnimationDrawable) imgView1.getBackground();
        anime2 = (AnimationDrawable) imgView2.getBackground();
        findViewById(R.id.img2).setBackgroundResource(R.drawable.d_); // ダミー

        mode = random.nextInt(8);
        findViewById(tvIds[mode]).setBackgroundColor(Color.RED);

        autoSW = findViewById(R.id.switch1);
        autoSW.setTextColor(Color.GRAY);
        autoSW.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if (autoSW.isChecked()) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        auto = () -> {
                            shuffle();
                            mHandler0.removeCallbacks(auto);
                            mHandler0.postDelayed(auto, 1500);
                        };
                        autoSW.setTextColor(Color.RED);
                        shuffle();
                        mHandler0.postDelayed(auto, 1500);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        autoSW.setTextColor(Color.GRAY);
                        mHandler0.removeCallbacks(auto);
                    }
                }
        );
    }

    private void drawing() {
        int x;

        Log.w("drawing", String.format("mode=%d", mode));
        if (mode == 0) {    // 天国モード
            x = random.nextInt(13);
            Log.w("drawing", String.format("result=%d", x));
            if (x == 0) {
                findViewById(tvIds[0]).setBackgroundColor(Color.WHITE);
                mode = random.nextInt(8);
                Log.w("drawing", String.format("next mode=%d", mode));
                findViewById(tvIds[mode]).setBackgroundColor(Color.RED);
                digit = reach();
            } else {
                x = random.nextInt(60);
                Log.w("drawing", String.format("result=%d", x));
                if (x < 10) {
                    digit = hit();
                } else if (x < 30) {
                    digit = reach();
                } else {
                    digit = failure();
                }
            }
        } else {    // 1-7 地獄モード
            x = random.nextInt(13);
            Log.w("drawing", String.format("result=%d", x));
            if (x == 0) {
                findViewById(tvIds[mode]).setBackgroundColor(Color.WHITE);
                mode = random.nextInt(8);
                Log.w("drawing", String.format("next mode=%d", mode));
                findViewById(tvIds[mode]).setBackgroundColor(Color.RED);
                digit = reach();
            } else {
                digit = failure();
            }
        }
    }
    private ResultNum hit() {
        ResultNum r = new ResultNum();
        if (random.nextInt(2) == 0) {
            r.d1 = r.d2 = 3;
        } else {
            r.d1 = r.d2 = 7;
        }
        return r;
    }
    private ResultNum failure() {
        ResultNum r = new ResultNum();
        int x = random.nextInt(10);
        if (x == 3 || x == 7) {
            x++;
        }
        r.d1 = x;
        r.d2 = random.nextInt(10);
        return r;
    }
    private ResultNum reach() {
        ResultNum r = new ResultNum();
        int x = random.nextInt(2);
        if (x == 0) {
            r.d1 = 3;
        } else {
            r.d1 = 7;
        }
        x = random.nextInt(10);
        if (r.d1 == 3 && x == 3) {
            r.d2 = 4;
        } else if (r.d1 == 7 && x == 7) {
            r.d2 = 8;
        } else {
            r.d2 = x;
        }
        return r;
    }
    private void digitPrint(ImageView v, int n) {
        v.setBackgroundResource(digitImages[n]);
    }

    private void shuffle() {
        drawing();  // 次の目を決める
        imgView1.setBackgroundResource(R.drawable.digit_list);
        imgView2.setBackgroundResource(R.drawable.digit_list);
        anime1 = (AnimationDrawable) imgView1.getBackground();
        anime2 = (AnimationDrawable) imgView2.getBackground();

        if (anime1.isRunning()) anime1.stop();
        anime1.start();
        if (anime2.isRunning()) anime2.stop();
        anime2.start();

        stop1 = () -> {
            if (anime1.isRunning()) {
                anime1.stop();
                digitPrint(imgView1, digit.d1);
                if (digit.d1 == 3 || digit.d1 == 7) {
                    soundPool.play(soundReach, 1.0f, 1.0f, 0, 0, 1);
                } else {
                    soundPool.play(soundNormal, 1.0f, 1.0f, 0, 0, 1);
                }
            }
            mHandler1.removeCallbacks(stop1);
        };
        mHandler1.postDelayed(stop1, 600);
        stop2 = () -> {
            if (anime2.isRunning()) {
                anime2.stop();
                digitPrint(imgView2, digit.d2);
                if (digit.d1 == 3 && digit.d2 == 3 || digit.d1 == 7 && digit.d2 == 7) {
                    soundPool.play(soundFanfare, 1.0f, 1.0f, 0, 0, 1);
                    imgView1.setBackgroundResource(digit.d1 == 3 ? R.drawable.fever3 : R.drawable.fever7);
                    imgView2.setBackgroundResource(digit.d1 == 3 ? R.drawable.fever3 : R.drawable.fever7);
                    anime1 = (AnimationDrawable) imgView1.getBackground();
                    anime2 = (AnimationDrawable) imgView2.getBackground();
                    if (anime1.isRunning()) anime1.stop();
                    anime1.start();
                    if (anime2.isRunning()) anime2.stop();
                    anime2.start();
                    if (autoSW.isChecked()) {
                        mHandler0.removeCallbacks(auto);
                        mHandler0.postDelayed(auto, 4000);
                    }
                } else {
                    soundPool.play(soundNormal, 1.0f, 1.0f, 0, 0, 1);
                }
            }
            mHandler2.removeCallbacks(stop2);
        };
        mHandler2.postDelayed(stop2, 900);
    }

    public void onClick(View v) {
        int x = v.getId();
        if (x == R.id.btn1) {
            shuffle();
        } else if (x == R.id.btn2) {
            findViewById(tvIds[mode]).setBackgroundColor(Color.WHITE);
            mode = 0;
            findViewById(tvIds[mode]).setBackgroundColor(Color.RED);
            soundPool.play(soundReach, 1.0f, 1.0f, 0, 0, 1);
        } else if (x == R.id.btn3) {
            soundPool.play(soundNormal, 1.0f, 1.0f, 0, 0, 1);
        } else if (x == R.id.btn4) {
            soundPool.play(soundReach, 1.0f, 1.0f, 0, 0, 1);
        } else if (x == R.id.btn5) {
            soundPool.play(soundFanfare, 1.0f, 1.0f, 0, 0, 1);
        } else if (x == R.id.btn6) {
            soundPool.play(soundFever, 1.0f, 1.0f, 0, 0, 1);
        }

        /* if-else 文に変更
        switch (v.getId()) {
            case R.id.btn1:
                shuffle();
                break;
            case R.id.btn2:
                findViewById(tvIds[mode]).setBackgroundColor(Color.WHITE);
                mode = 0;
                findViewById(tvIds[mode]).setBackgroundColor(Color.RED);
                soundPool.play(soundReach, 1.0f, 1.0f, 0, 0, 1);
                break;
            case R.id.btn3:
                soundPool.play(soundNormal, 1.0f, 1.0f, 0, 0, 1);
                break;
            case R.id.btn4:
                soundPool.play(soundReach, 1.0f, 1.0f, 0, 0, 1);
                break;
            case R.id.btn5:
                soundPool.play(soundFanfare, 1.0f, 1.0f, 0, 0, 1);
                break;
            case R.id.btn6:
                soundPool.play(soundFever, 1.0f, 1.0f, 0, 0, 1);
                break;
            default:
                break;
        }
         */
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about_title)
                    .setMessage(R.string.about_body)
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        }

        return true;
    }
}