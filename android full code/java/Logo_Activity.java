package com.example.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Logo_Activity extends AppCompatActivity {
    int count;
    private ImageView imgAndroid;
    private Animation anim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_);
        imgAndroid = (ImageView) findViewById(R.id.loding_img);
        initView();

        count = 1500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Logo_Activity.this, Main_Activity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        },count);
    }

    public void initView(){

        anim = AnimationUtils.loadAnimation(this, R.anim.dust_loding);
        imgAndroid.setAnimation(anim);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearApplicationCache(null);
    }

    //파일 캐시 삭제
    private void clearApplicationCache(java.io.File dir){
        if(dir == null){
            dir = getCacheDir();
            return;
        }
        java.io.File[] dirs = dir.listFiles();
        try{
            for (int i = 0; i < dirs.length ; i++){
                if(dirs[i].isDirectory())
                    clearApplicationCache(dirs[i]);
                else
                    dirs[i].delete();
            }
        }catch (Exception e){
        }
    }
}
