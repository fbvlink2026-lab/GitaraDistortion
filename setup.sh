#!/bin/bash
set -e

echo "=== GUMAGAWA NG MGA FOLDER ==="
mkdir -p app/src/main/cpp
mkdir -p app/src/main/java/com/gitaradistortion
mkdir -p app/src/main/res/layout
mkdir -p .github/workflows

echo "=== settings.gradle.kts ==="
cat > settings.gradle.kts << 'EOF'
pluginManagement {
    repositories { google(), mavenCentral(), gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(), mavenCentral() }
}
rootProject.name = "GitaraDistortion"
include(":app")
EOF

echo "=== build.gradle.kts ==="
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
EOF

echo "=== gradle.properties ==="
cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
EOF

echo "=== app/build.gradle.kts ==="
cat > app/build.gradle.kts << 'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.gitaradistortion"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.gitaradistortion"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        externalNativeBuild { cmake { arguments("-DANDROID_STL=c++_shared") } }
    }
    buildFeatures { prefab = true }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_1_8; targetCompatibility = JavaVersion.VERSION_1_8 }
    kotlinOptions { jvmTarget = "1.8" }
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.oboe:oboe:1.9.0")
}
EOF

echo "=== AndroidManifest.xml ==="
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <application android:allowBackup="true" android:icon="@mipmap/ic_launcher" android:label="Gitara Distortion" android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true" android:screenOrientation="portrait">
            <intent-filter><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent-filter>
        </activity>
    </application>
</manifest>
EOF

echo "=== CMakeLists.txt ==="
cat > app/src/main/cpp/CMakeLists.txt << 'EOF'
cmake_minimum_required(VERSION 3.22.1)
project("gitaradistortion")
add_library(gitaradistortion SHARED AudioEngine.cpp)
find_package(oboe REQUIRED CONFIG)
target_link_libraries(gitaradistortion oboe::oboe android log)
EOF

echo "=== AudioEngine.cpp ==="
cat > app/src/main/cpp/AudioEngine.cpp << 'EOF'
#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)
static float gVol=0.5f,gTone=0.5f,gDist=2.0f,prevSmp=0.0f;
static std::shared_ptr<oboe::AudioStream> stream;
class DistCb:public oboe::AudioStreamCallback{
public:
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream*,void* d,int32_t n)override{
        float* b=static_cast<float*>(d);
        for(int i=0;i<n;i++){
            float in=b[i];
            float p=std::tanh(in*gDist);
            if(gTone<1.0f){p=p*gTone+prevSmp*(1.0f-gTone);prevSmp=p;}
            b[i]=p*gVol*0.8f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};
static DistCb cb;
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*,jobject){
    oboe::AudioStreamBuilder b;
    b.setDirection(oboe::Direction::InputOutput)->setSampleRate(44100)->setFormat(oboe::AudioFormat::Float)->setPerformanceMode(oboe::PerformanceMode::LowLatency)->setCallback(&cb);
    if(b.openStream(stream)==oboe::Result::OK)stream->requestStart(),LOGI("✅ NAKA-ON!");
}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*,jobject){
    if(stream)stream->stop(),stream->close(),stream.reset(),LOGI("⏹️ NAKA-OFF");
}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setVolume(JNIEnv*,jobject,jfloat v){gVol=v;}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setTone(JNIEnv*,jobject,jfloat v){gTone=v;}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setDistortion(JNIEnv*,jobject,jfloat v){gDist=v;}
EOF

echo "=== MainActivity.kt ==="
cat > app/src/main/java/com/gitaradistortion/MainActivity.kt << 'EOF'
package com.gitaradistortion
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.*
class KnobView(ctx:android.content.Context):View(ctx){
    var value=0.5f
        set(v){field=v.coerceIn(0f,1f);invalidate()}
    private val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    var onChange:((Float)->Unit)?=null
    var color=0xFFFF6622.toInt()
    override fun onDraw(c:android.graphics.Canvas){
        val w=width.toFloat();val h=height.toFloat()
        val cx=w/2;val cy=h/2;val r=minOf(w,h)/2-12f
        paint.style=android.graphics.Paint.Style.FILL;paint.color=0xFF2A2A30.toInt()
        c.drawCircle(cx,cy,r,paint)
        paint.style=android.graphics.Paint.Style.STROKE;paint.strokeWidth=6f;paint.color=color
        c.drawCircle(cx,cy,r-3f,paint)
        val ang=-135f+(270f)*value
        val rad=Math.toRadians(ang.toDouble())
        paint.strokeWidth=5f;paint.color=color
        val ex=cx+(r-25f)*sin(rad).toFloat()
        val ey=cy-(r-25f)*cos(rad).toFloat()
        c.drawLine(cx,cy,ex,ey,paint)
        paint.style=android.graphics.Paint.Style.FILL;paint.color=color
        c.drawCircle(cx,cy,12f,paint)
    }
    private var startAng=0.0
    override fun onTouchEvent(e:MotionEvent):Boolean{
        val cx=width/2f;val cy=height/2f
        val ang=Math.toDegrees(atan2(e.x-cx,cy-e.y).toDouble())
        when(e.action){
            MotionEvent.ACTION_DOWN->startAng=ang
            MotionEvent.ACTION_MOVE->{
                var d=ang-startAng
                if(d>180)d-=360.0
                if(d<-180)d+=360.0
                value+=(d/270.0).toFloat()
                onChange?.invoke(value)
                startAng=ang
            }
        }
        return true
    }
}
class MainActivity:AppCompatActivity(){
    companion object{init{System.loadLibrary("gitaradistortion")}}
    external fun startAudioEngine()
    external fun stopAudioEngine()
    external fun setVolume(v:Float)
    external fun setTone(v:Float)
    external fun setDistortion(v:Float)
    private var run=false
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        setContentView(R.layout.activity_main)
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),100)
        val st=findViewById<TextView>(R.id.statusText)
        val vt=findViewById<TextView>(R.id.volText)
        val tt=findViewById<TextView>(R.id.toneText)
        val dt=findViewById<TextView>(R.id.distText)
        val vb=findViewById<KnobView>(R.id.volKnob)
        val tb=findViewById<KnobView>(R.id.toneKnob)
        val db=findViewById<KnobView>(R.id.distKnob)
        val pwr=findViewById<Button>(R.id.powerBtn)
        val emg=findViewById<Button>(R.id.emgBtn)
        vb.onChange={setVolume(it);vt.text="🔊 VOLUME: ${(it*100).toInt()}%"}
        tb.onChange={val v=0.05f+it*2.95f;setTone(v);tt.text="🎵 TONE: ${(it*100).toInt()}%"}
        db.onChange={val v=0.5f+it*9.5f;setDistortion(v);dt.text="💥 DIST: %.1fx".format(v)}
        pwr.setOnClickListener{
            if(!run){startAudioEngine();run=true;pwr.text="🟢 TURN OFF";pwr.setBackgroundColor(0xFFFF4444.toInt());st.text="🟢 NAKA-ON! Tumugtog!";st.setTextColor(0xFF44FF44.toInt())}
            else{stopAudioEngine();run=false;pwr.text="🔴 TURN ON";pwr.setBackgroundColor(0xFF228833.toInt());st.text="⚪ NAKA-OFF — Isaksak iRig";st.setTextColor(0xFF888888.toInt())}
        }
        emg.setOnClickListener{
            stopAudioEngine();run=false
            vb.value=0f;tb.value=0.5f;db.value=0.5f
            setVolume(0f)
            vt.text="🔊 VOLUME: 0%";tt.text="🎵 TONE: 50%";dt.text="💥 DIST: 5.3x"
            pwr.text="🔴 TURN ON";pwr.setBackgroundColor(0xFF228833.toInt())
            st.text="🚨 EMERGENCY — LAHAT TUMIGIL";st.setTextColor(0xFFFF4444.toInt())
        }
    }
}
EOF

echo "=== activity_main.xml ==="
cat > app/src/main/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="20dp"
    android:background="#141418" android:gravity="center_horizontal">
    <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="🎸 GITARA DISTORTION" android:textSize="26sp"
        android:textStyle="bold" android:textColor="#FF6622" android:layout_margin="8dp"/>
    <TextView android:id="@+id/statusText" android:layout_width="match_parent"
        android:layout_height="wrap_content" android:text="⚪ Isaksak iRig → Pihitan at Pindutin ON"
        android:textSize="15sp" android:gravity="center" android:padding="10dp"
        android:background="#222228" android:textColor="#888888" android:layout_marginBottom="16dp"/>
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="16dp">
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:orientation="vertical" android:gravity="center">
            <TextView android:id="@+id/volText" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:text="🔊 VOLUME: 50%"
                android:textSize="13sp" android:textColor="#FF9944" android:textStyle="bold"/>
            <com.gitaradistortion.KnobView android:id="@+id/volKnob"
                android:layout_width="90dp" android:layout_height="90dp"/>
        </LinearLayout>
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:orientation="vertical" android:gravity="center">
            <TextView android:id="@+id/toneText" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:text="🎵 TONE: 50%"
                android:textSize="13sp" android:textColor="#44DDAA" android:textStyle="bold"/>
            <com.gitaradistortion.KnobView android:id="@+id/toneKnob"
                android:layout_width="90dp" android:layout_height="90dp"/>
        </LinearLayout>
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_weight="1" android:orientation="vertical" android:gravity="center">
            <TextView android:id="@+id/distText" android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:text="💥 DIST: 5.3x"
                android:textSize="13sp" android:textColor="#FF4444" android:textStyle="bold"/>
            <com.gitaradistortion.KnobView android:id="@+id/distKnob"
                android:layout_width="90dp" android:layout_height="90dp"/>
        </LinearLayout>
    </LinearLayout>
    <Button android:id="@+id/powerBtn" android:layout_width="match_parent"
        android:layout_height="65dp" android:text="🔴 TURN ON" android:textSize="22sp"
        android:textStyle="bold" android:backgroundColor="#228833" android:textColor="#FFFFFF"
        android:layout_marginHorizontal="10dp"/>
    <Button android:id="@+id/emgBtn" android:layout_width="match_parent"
        android:layout_height="75dp" android:text="🚨 EMERGENCY OFF\nITIGIL AGAD!"
        android:textSize="19sp" android:textStyle="bold" android:backgroundColor="#CC1111"
        android:textColor="#FFFF66" android:layout_marginHorizontal="10dp" android:layout_marginTop="10dp"/>
</LinearLayout>
EOF

echo "✅ LAHAT NG FILE — NABUO NA!"
ls -la
