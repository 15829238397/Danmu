package com.example.pengbo.danmu;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.File;
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    private VideoView vvViodeView ;
    private Button butPlay ;

    private View decorView ;

    private LinearLayout operationlayout ;
    private Button butSend ;
    private EditText editText ;

    private MediaController controller ;
    private DanmakuView danmakuView ;
    private DanmakuContext danmakuContext ;
    private boolean showDanmaku ;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        initView() ;
        init() ;

        Log.d("onCreate" , "－－－－－－－－－－－－－－－－－－－－－－运行结束－－－－－－－－－－－－") ;

    }

    private void initView(){

        vvViodeView = this.findViewById(R.id.vid_main) ;
        butPlay = this.findViewById(R.id.but_play) ;
        danmakuView = this.findViewById(R.id.danmaku_view) ;

        operationlayout = findViewById(R.id.operation_layout);
        butSend = findViewById(R.id.but_send) ;
        editText = findViewById(R.id.edit_text) ;

        decorView = getWindow().getDecorView() ;

        //全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus && Build.VERSION.SDK_INT >= 19){
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    private void init(){

        controller = new MediaController(this) ;

        addListener() ;

    }

    /**
     * 向弹幕View中添加一条弹幕
     * @param content
     *          弹幕的具体内容
     * @param  withBorder
     *          弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        danmakuView.addDanmaku(danmaku);
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }




    /**
     * 初始化弹幕库设置
     */
    private void initDanmaku(){

        danmakuView.enableDanmakuDrawingCache(true);
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                danmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });

        danmakuContext = DanmakuContext.create();
        danmakuView.prepare(parser, danmakuContext);

    }

    /**
     * 随机生成弹幕
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void addListener (){

        //播放按钮监听
        butPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("addListener" , "-----------点击播放时间") ;

                playMv() ;
                initDanmaku() ;

                //隐藏播放按钮
                butPlay.setVisibility(View.GONE);
            }
        });

        //点击放映屏幕，进行弹幕发送界面弹出返回
        danmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (operationlayout.getVisibility() == View.GONE){
                    operationlayout.setVisibility(View.VISIBLE);
                }else{
                    operationlayout.setVisibility(View.GONE);
                }
            }
        });

        //点击发送按钮进行弹幕发送
        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString() ;
                if(!TextUtils.isEmpty(content)){
                    addDanmaku(content , true);
                    editText.setText("");
                }
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if(i == View.SYSTEM_UI_FLAG_VISIBLE){
                    onWindowFocusChanged(true);
                }
            }
        });

    }



    /**
     * 定义播放功能，这里暂时只播放本地一个固定的文件.
     * 后续添加播放文件夹下所有视频文件功能.
     */
    private void playMv(){

        File viewFile = new File(
                Environment.getExternalStorageDirectory().getPath() +
                "/data/1.mp4") ;

        if(viewFile.exists()){

            vvViodeView.setVideoPath(viewFile.getAbsolutePath()) ;
            vvViodeView.setMediaController(controller) ;
            vvViodeView.start() ;
            vvViodeView.requestFocus() ;
            //将焦点固定在放映页面

        }else{

            Log.d(" playMv","要播放文件不存在") ;

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }

}
