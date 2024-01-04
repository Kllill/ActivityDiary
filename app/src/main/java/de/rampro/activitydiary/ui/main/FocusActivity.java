/*
 * ActivityDiary
 *
 * Copyright (C) 2023 Raphael Mack http://www.raphael-mack.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.rampro.activitydiary.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.rampro.activitydiary.ActivityDiaryApplication;
import de.rampro.activitydiary.R;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.helpers.SharedPreferencesUtils;
import de.rampro.activitydiary.model.DetailViewModel;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.model.TimeUtil;
import de.rampro.activitydiary.ui.history.HistoryDetailActivity;
import de.rampro.activitydiary.ui.settings.SettingsActivity;

public class FocusActivity extends HdActivity {
    private final int IMAGE_REQUEST_CODE = 2000;

    private DiaryActivity currentActivity;
    private DetailViewModel viewModel;
    private String mCurrentPhotoPath;

    private ConstraintLayout rootLayout;

    TextView activityButton;
    TextView timeshow;
    EditText et;
    Button buttonBack;
    Button buttonStop;
    Button buttonTakePhoto;
    Button buttonAlbum;
    Button changeBackground;

    long count=0;

    int changephoto = 0;
    final static int UPDATE_TEXTVIEW=0;
    private boolean isImageMusicShowing = true;

    Handler handler=new Handler(Looper.getMainLooper()){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==UPDATE_TEXTVIEW){
                timeshow.setText(TimeUtil.getFormatTime(count));
            }
        }
    };

    Timer timer=null;
    TimerTask timerTask=null;
    boolean isPause=false;
    private void startTimer(){
        if (timer==null){
            timer=new Timer();
        }

        if(timerTask==null){
            timerTask=new TimerTask() {
                @Override
                public void run() {
                    Message message=new Message();
                    message.what=UPDATE_TEXTVIEW;
                    handler.sendMessage(message);
                    do{
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }while(isPause);
                    count++;
                }
            };
        }

        if(timerTask!=null&&timer!=null){
            timer.schedule(timerTask,1000,1000);
        }
    }


    //musicbutton相关申明
    Button musicButton;
    public static int cnt = 0;
    SoundPool sp;//声明SoundPool的引用
    HashMap<Integer, Integer> hm;//声明HashMap来存放声音文件
    int currStaeamId;//当前正播放的streamId

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("currentPhotoPath", mCurrentPhotoPath);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
    private ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result){
                //这里处理图片
            }
        }
    });
    private ActivityResultLauncher<String> getImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            //这里处理图片
            //选择图片是 result 这个Uri
        }
    });

    private Uri uri;    //拍照之后这个Uri可以获取到图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_detail);

        viewModel = ViewModelProviders.of(this).get(DetailViewModel.class);

        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString("currentPhotoPath");
        }

        Intent i = getIntent();
        int actId = i.getIntExtra("activityID", -1);
        if(actId == -1) {
            currentActivity = null;
        }else {
            currentActivity = ActivityHelper.helper.activityWithId(actId);
        }
        activityButton = findViewById(R.id.activity_title);
        activityButton.setText(currentActivity.getName());

        //获取时间戳，以此计算时间
        Date startdate = ActivityHelper.helper.getCurrentActivityStartTime();
        long currentTime = System.currentTimeMillis();
        long startTime;
        Date currentDate = new Date(currentTime);
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
        String startft = ft.format(startdate);
        String currentft = ft.format(currentDate);
        try {
            startTime = Long.parseLong(String.valueOf(ft.parse(startft).getTime()/1000));
            currentTime = Long.parseLong(String.valueOf(ft.parse(currentft).getTime()/1000));
            count = currentTime - startTime;
            Log.d("start stamp", String.valueOf(startTime));
            Log.d("start current stamp", String.valueOf(currentTime));
            Log.d("start - current",String.valueOf(currentTime-startTime));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        startTimer();
        timeshow = findViewById(R.id.timer);

        //返回按钮
        buttonBack = findViewById(R.id.button);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK);
                finish();
            }
        });

        //
        et = findViewById(R.id.et);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated methodstub
                //若接收到回车键时候失去焦点，隐藏输入法
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    et.clearFocus();
                    InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(v.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        //改变背景图片
        rootLayout = findViewById(R.id.rootlayout);
        changeBackground = findViewById(R.id.changeBack);
        changeBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (changephoto){
                    case 0:
                        rootLayout.setBackgroundResource(R.drawable.backgound2);
                        break;
                    case 1:
                        rootLayout.setBackgroundResource(R.drawable.backgound3);
                        break;
                    case 2:
                        rootLayout.setBackgroundResource(R.drawable.backgound4);
                        break;
                    case 3:
                        rootLayout.setBackgroundResource(R.drawable.backgound);
                        break;
                    default:
                        break;
                }
                changephoto = (changephoto+1)%4;
            }
        });

        //暂停按钮
        buttonStop = findViewById(R.id.stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPause = true;
                buttonStop.setBackgroundResource(R.drawable.stop_go);
                if(PreferenceManager
                        .getDefaultSharedPreferences(ActivityDiaryApplication.getAppContext())
                        .getBoolean(SettingsActivity.KEY_PREF_DISABLE_CURRENT, true)){
                    ActivityHelper.helper.setCurrentActivity(null);
                }else{
                    Intent i = new Intent(FocusActivity.this, HistoryDetailActivity.class);
                    // no diaryEntryID will edit the last one
                    startActivity(i);
                }
            }
        });


        //拍照按钮
        buttonTakePhoto = findViewById(R.id.take_photo);

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                takePhotoLauncher.launch(uri);
            }
        });
        buttonAlbum = findViewById(R.id.album);
        buttonAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  getImageLauncher.launch("image/*");
                if (ContextCompat.checkSelfPermission(FocusActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FocusActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    selectFromAlbum();
                }
            }
        });

        //musicbutton相关逻辑
        initSoundPool();
        musicButton = findViewById(R.id.music);
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击按钮播放音乐，再次点击停止播放
                if(isImageMusicShowing){
                    musicButton.setBackgroundResource(R.drawable.music_new);
                }else{
                    musicButton.setBackgroundResource(R.drawable.music);
                }
                isImageMusicShowing = !isImageMusicShowing;
                if(cnt==0){
                    cnt = 1;
                }
                else {
                    cnt = 0;
                }
                if(cnt==1) {
                    playSound(1, 0);//播放1号声音资源，且播放一次
                    //提示播放即时音效
                    Toast.makeText(FocusActivity.this, "播放音乐", Toast.LENGTH_SHORT).show();
                }
                else {
                    sp.stop(currStaeamId);//停止正在播放的某个声音
                    //提示停止播放
                    Toast.makeText(FocusActivity.this, "停止播放音乐", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void playSound(int sound, int loop) {//获取AudioManager引用
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //获取当前音量
        float streamVolumeCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        //获取系统最大音量
        float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //计算得到播放音量
        float volume = streamVolumeCurrent / streamVolumeMax;
        //调用SoundPool的play方法来播放声音文件
        currStaeamId = sp.play(hm.get(sound), volume, volume, 1, loop, 1.0f);
    }

    public void initSoundPool() {//初始化声音池
        sp = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);//创建SoundPool对象
        hm = new HashMap<Integer, Integer>();//创建HashMap对象
        //加载声音文件，并且设置为1号声音放入hm中
        hm.put(1, sp.load(this, R.raw.piano, 1));
    }

    private void selectFromAlbum() {
        Log.e("dashboard fragment","click img_profile:");

        Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent2, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == RESULT_OK) {
            final Intent intent = data;
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    Uri selectedImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String path = cursor.getString(columnIndex);
                    cursor.close();
                    Log.e("onActivityResult","path:"+path);
                    SharedPreferencesUtils helper = new SharedPreferencesUtils(this, "picture");
                    helper.putValues(new SharedPreferencesUtils.ContentValue("pic"+1,path));
                    break;
            }
        } else {
            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}