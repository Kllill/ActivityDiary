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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.rampro.activitydiary.R;
import de.rampro.activitydiary.helpers.ActivityHelper;
import de.rampro.activitydiary.model.DiaryActivity;
import de.rampro.activitydiary.model.TimeUtil;
import de.rampro.activitydiary.ui.generic.BaseActivity;

public class FocusActivity extends BaseActivity {

    private DiaryActivity currentActivity;

    TextView activityButton;
    TextView timeshow;
    Button buttonBack;

    long count=0;
    final static int UPDATE_TEXTVIEW=0;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_detail);

        Intent i = getIntent();
        int actId = i.getIntExtra("activityID", -1);
        if(actId == -1) {
            currentActivity = null;
        }else {
            currentActivity = ActivityHelper.helper.activityWithId(actId);
        }

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
        activityButton = findViewById(R.id.activity_title);
        activityButton.setText(currentActivity.getName());

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

        timeshow = findViewById(R.id.timer);

    }
}