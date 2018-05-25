package com.example.arpita.dodgegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    SensorManager sensorManager;
    Sensor sensor;
    int x=100,y=0,score=0,pastX=0;
    boolean speed=false, hit=false, gameOver=false,first=true;
    int startTime,currentTime,timeLeft,oldTime=-100,newTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        sensorManager = (SensorManager)(getSystemService(SENSOR_SERVICE));
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            x = (int) -(100 * event.values[0]);
            if (Math.abs(x-pastX)>=15){
                x+=3;
            }else if (Math.abs(x-pastX)>=30){
                x+=5;
            }
            else if (Math.abs(x-pastX)<=15){
                x=pastX;
            }
            pastX = x;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(MainActivity.this);
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        gameSurface.resume();
    }




    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap myImage,monster,broken;
        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        int rand;
        Rect monsterHit = new Rect(rand,y,rand+100,y+100);
        Rect imageHit = new Rect(x,1200,x+100,1300);

        public GameSurface(Context context) {
            super(context);

            startTime = (int) System.currentTimeMillis();
            System.out.println(startTime);

            holder=getHolder();

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.piggybank);
            monster = BitmapFactory.decodeResource(getResources(),R.drawable.hammer);
            broken = BitmapFactory.decodeResource(getResources(),R.drawable.brokenpiggy);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
        }

            @Override
            public boolean onTouchEvent(MotionEvent event){
                if (first && speed==true){
                    speed=false;
                }
                if (gameOver){
                    first=true;
                    speed=false;
                    hit=false;
                    y=0;
                    rand = (int)(Math.random()*500);
                    startTime=(int)System.currentTimeMillis();
                    gameOver=false;
                    oldTime = (int) System.currentTimeMillis();
                    newTime = (int) System.currentTimeMillis();
                }
                else if (!gameOver) {

                    //System.out.println(""+speed);
                    //System.out.println(""+(newTime - oldTime));
                    int tim = (int)(Math.abs(newTime - oldTime));
                    if (first && tim >= 10 && speed==false) {
                        speed = true;
                        first = false;
                        System.out.println("First "+speed);
                    }
                    else if (!first) {
                        System.out.println("Not first "+speed);
                        newTime = (int) (System.currentTimeMillis());
                        if (newTime - oldTime >= 100) {

                            if (!speed) {
                                speed = true;
                            } else if (speed) {
                                speed = false;
                            }
                        }
                    }
                }
                oldTime = (int) System.currentTimeMillis();
                return true;
            }

        @Override
        public void run() {
            while (running){

                currentTime = (int)System.currentTimeMillis();
                timeLeft=30-(currentTime-startTime)/1000;

                if (currentTime-startTime>=30000){
                    gameOver=true;
                }

                if (!holder.getSurface().isValid())
                    continue;

                if (getResources().getConfiguration().orientation==2){
                    sensorManager.unregisterListener(MainActivity.this);
                }

                Canvas canvas= holder.lockCanvas();
                canvas.drawRGB(0,128,128);
                canvas.drawText("Score "+score,50,200,paintProperty);
                int value=0;

                if (!gameOver) {

                    value = x + 500;
                    if (value >= 960) {
                        value = 960;
                    } else if (value <= 60) {
                        value = 60;
                    }
                    if (!speed) {
                        y += 7;
                    } else {
                        y += 20;
                    }
                    if (y > 1600 && !hit) {
                        score++;
                        rand = (int) (Math.random() * 800);
                        y = 0;
                    } else if (y > 1600 && hit) {
                        rand = (int) (Math.random() * 800);
                        y = 0;
                        hit = false;
                    }
                    monsterHit = new Rect(rand, y, rand + 250, y + 200);
                    imageHit = new Rect(value, 1200, value + 200, 1200 + 100);
                    if (monsterHit.intersect(imageHit) && !hit) {
                        score--;
                        MediaPlayer glass= MediaPlayer.create(MainActivity.this,R.raw.glass);
                        glass.start();
                        hit = true;
                    }
                }

                canvas.drawBitmap(monster, rand, y, null);

                if (!hit) {
                    canvas.drawBitmap(myImage, value, 1200, null);
                }
                else if (hit){
                    canvas.drawBitmap(broken, value-80, 1200, null);//broken image
                }
                canvas.drawText("Time Left: "+timeLeft,580,200,paintProperty);

                if (gameOver){
                    canvas.drawRGB(150,150,150);
                    int s = score;
                    canvas.drawText("GAME OVER",330,800,paintProperty);
                    canvas.drawText("Score "+s,430,600,paintProperty);
                    canvas.drawText("Tap anywhere to restart",75,1200,paintProperty);
                    score=0;
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            sensorManager.unregisterListener(MainActivity.this);
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GameSurface
}//Activity



