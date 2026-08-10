#!/bin/bash
# =====================================================
# 🎸 MartoDosko — GITARA DISTORTION — BILOG NA KNOB VERSION
# C++ + Oboe = WALANG DELAY! Katulad ng Tonebridge!
# =====================================================

set -e

echo ""
echo "🎸 ==================================="
echo "   BILOG NA KNOB — AWTOMATIKONG BUILDER"
echo "   C++ + OBOE — WALANG DELAY!"
echo "========================================"
echo ""

# =====================================================
# 📁 Gumawa ng mga folder
# =====================================================
echo "📁 Gumagawa ng mga folder..."
mkdir -p app/src/main/cpp
mkdir -p app/src/main/java/com/gitaradistortion
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/drawable
mkdir -p .github/workflows
echo "✅ Mga folder — TAPOS!"

# =====================================================
# 📄 settings.gradle.kts
# =====================================================
echo "📄 Gumagawa ng settings.gradle.kts..."
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

# =====================================================
# 📄 build.gradle.kts (ugat)
# =====================================================
echo "📄 Gumagawa ng build.gradle.kts..."
cat > build.gradle.kts << 'EOF'
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
EOF

# =====================================================
# 📄 gradle.properties
# =====================================================
echo "📄 Gumagawa ng gradle.properties..."
cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
EOF

# =====================================================
# 📄 app/build.gradle.kts
# =====================================================
echo "📄 Gumagawa ng app/build.gradle.kts..."
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
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

# =====================================================
# 📄 AndroidManifest.xml
# =====================================================
echo "📄 Gumagawa ng AndroidManifest.xml..."
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Gitara Distortion"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# =====================================================
# 📄 CMakeLists.txt
# =====================================================
echo "📄 Gumagawa ng CMakeLists.txt..."
cat > app/src/main/cpp/CMakeLists.txt << 'EOF'
cmake_minimum_required(VERSION 3.22.1)
project("gitaradistortion")
add_library(gitaradistortion SHARED AudioEngine.cpp)
find_package(oboe REQUIRED CONFIG)
target_link_libraries(gitaradistortion oboe::oboe android log)
EOF

# =====================================================
# 📄 AudioEngine.cpp — WALANG PAGBABAGO
# =====================================================
echo "📄 Gumagawa ng AudioEngine.cpp..."
cat > app/src/main/cpp/AudioEngine.cpp << 'EOF'
#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)
static float gVolume = 0.5f, gTone = 0.5f, gDistortion = 2.0f, prevSample = 0.0f;
static std::shared_ptr<oboe::AudioStream> stream;

class DistortionCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream*, void* data, int32_t n) override {
        float* buf = static_cast<float*>(data);
        for(int i=0; i<n; i++) {
            float in = buf[i];
            float proc = std::tanh(in * gDistortion);
            if(gTone < 1.0f) { proc = proc*gTone + prevSample*(1.0f-gTone); prevSample=proc; }
            buf[i] = proc * gVolume * 0.8f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};
static DistortionCallback cb;

extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    oboe::AudioStreamBuilder b;
    b.setDirection(oboe::Direction::InputOutput)->setSampleRate(44100)
     ->setFormat(oboe::AudioFormat::Float)->setPerformanceMode(oboe::PerformanceMode::LowLatency)
     ->setCallback(&cb);
    if(b.openStream(stream)==oboe::Result::OK) stream->requestStart(), LOGI("✅ NAKA-ON!");
}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if(stream) stream->stop(), stream->close(), stream.reset(), LOGI("⏹️ NAKA-OFF");
}
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setVolume(JNIEnv*, jobject, jfloat v) { gVolume=v; }
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setTone(JNIEnv*, jobject, jfloat v) { gTone=v; }
extern "C" JNIEXPORT void JNICALL Java_com_gitaradistortion_MainActivity_setDistortion(JNIEnv*, jobject, jfloat v) { gDistortion=v; }
EOF

# =====================================================
# 📄 MainActivity.kt — MAY BILOG NA KNOB!
# =====================================================
echo "📄 Gumagawa ng MainActivity.kt..."
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

class KnobView(context: android.content.Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f,1f); invalidate() }
    var minAngle = -135f
    var maxAngle = 135f
    private var startAngle = 0f
    var onValueChange: ((Float) -> Unit)? = null
    var labelText = ""
    var valueText = ""
    var color = 0xFFFF6622.toInt()

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private val txtPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w/2
        val cy = h/2
        val r = minOf(w,h)/2 - 12f

        // Bilog na background
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF2A2A30.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // Gilid
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = color
        canvas.drawCircle(cx, cy, r - 3f, paint)

        // Sukatan (marka)
        paint.strokeWidth = 2.5f
        paint.color = 0xFF888888.toInt()
        for(i in 0..10) {
            val deg = minAngle + (maxAngle - minAngle) * i/10f
            val rad = Math.toRadians(deg.toDouble())
            val x1 = cx + (r - 18f) * sin(rad).toFloat()
            val y1 = cy - (r - 18f) * cos(rad).toFloat()
            val x2 = cx + (r - 8f) * sin(rad).toFloat()
            val y2 = cy - (r - 8f) * cos(rad).toFloat()
            canvas.drawLine(x1,y1,x2,y2,paint)
        }

        // Pihitan na linya
        val angle = minAngle + (maxAngle - minAngle) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f
        paint.color = color
        val xEnd = cx + (r - 25f) * sin(rad).toFloat()
        val yEnd = cy - (r - 25f) * cos(rad).toFloat()
        canvas.drawLine(cx, cy, xEnd, yEnd, paint)

        // Gitna ng knob
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = color
        canvas.drawCircle(cx, cy, 12f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width/2f
        val cy = height/2f
        val angle = Math.toDegrees(atan2(event.x - cx, cy - event.y).toDouble())
        when(event.action) {
            MotionEvent.ACTION_DOWN -> { startAngle = angle.toFloat() }
            MotionEvent.ACTION_MOVE -> {
                var delta = angle - startAngle
                if (delta > 180) delta -= 360.0
                if (delta < -180) delta += 360.0
                val range = maxAngle - minAngle
                value += (delta / range).toFloat()
                value = value.coerceIn(0f,1f)
                onValueChange?.invoke(value)
                startAngle = angle.toFloat()
            }
        }
        return true
    }
}

class MainActivity : AppCompatActivity() {
    companion object {
        init { System.loadLibrary("gitaradistortion") }
    }
    external fun startAudioEngine()
    external fun stopAudioEngine()
    external fun setVolume(v:Float)
    external fun setTone(v:Float)
    external fun setDistortion(v:Float)

    private var isRunning = false
    private lateinit var statusText: TextView
    private lateinit var volumeText: TextView
    private lateinit var toneText: TextView
    private lateinit var distText: TextView
    private lateinit var powerBtn: Button
    private lateinit var emergencyBtn: Button
    private lateinit var volKnob: KnobView
    private lateinit var toneKnob: KnobView
    private lateinit var distKnob: KnobView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),100)
        }

        statusText = findViewById(R.id.statusText)
        volumeText = findViewById(R.id.volumeText)
        toneText = findViewById(R.id.toneText)
        distText = findViewById(R.id.distortionText)
        powerBtn = findViewById(R.id.powerBtn)
        emergencyBtn = findViewById(R.id.emergencyBtn)
        volKnob = findViewById(R.id.volKnob)
        toneKnob = findViewById(R.id.toneKnob)
        distKnob = findViewById(R.id.distKnob)

        // 🔊 VOLUME KNOB
        volKnob.color = 0xFFFF8822.toInt()
        volKnob.value = 0.5f
        volKnob.onValueChange = { v ->
            setVolume(v)
            volumeText.text = "🔊 VOLUME: ${(v*100).toInt()}%"
        }

        // 🎵 TONE KNOB
        toneKnob.color = 0xFF22DD88.toInt()
        toneKnob.value = 0.5f
        toneKnob.onValueChange = { v ->
            val toneVal = 0.05f + v * 2.95f
            setTone(toneVal)
            toneText.text = "🎵 TONE: ${(v*100).toInt()}%"
        }

        // 💥 DISTORTION KNOB
        distKnob.color = 0xFFFF3333.toInt()
        distKnob.value = 0.5f
        distKnob.onValueChange = { v ->
            val distVal = 0.5f + v * 9.5f
            setDistortion(distVal)
            distText.text = "💥 DIST: %.1fx".format(distVal)
        }

        // 🟢🔴 POWER BUTTON
        powerBtn.setOnClickListener {
            if(!isRunning) {
                startAudioEngine()
                isRunning = true
                powerBtn.text = "🟢 TURN OFF"
                powerBtn.setBackgroundColor(0xFFFF4444.toInt())
                statusText.text = "🟢 NAKA-ON! Tumugtog ka na!"
                statusText.setTextColor(0xFF44FF44.toInt())
            } else {
                stopAudioEngine()
                isRunning = false
                powerBtn.text = "🔴 TURN ON"
                powerBtn.setBackgroundColor(0xFF228833.toInt())
                statusText.text = "⚪ NAKA-OFF — Isaksak iRig"
                statusText.setTextColor(0xFF888888.toInt())
            }
        }

        // 🚨 EMERGENCY BUTTON
        emergencyBtn.setOnClickListener {
            stopAudioEngine()
            isRunning = false
            volKnob.value = 0f; setVolume(0f)
            toneKnob.value = 0.5f
            distKnob.value = 0.5f
            volumeText.text = "🔊 VOLUME: 0%"
            toneText.text = "🎵 TONE: 50%"
            distText.text = "💥 DIST: 5.3x"
            powerBtn.text = "🔴 TURN ON"
            powerBtn.setBackgroundColor(0xFF228833.toInt())
            statusText.text = "🚨 EMERGENCY — LAHAT TUMIGIL"
            statusText.setTextColor(0xFFFF4444.toInt())
        }
    }
}
EOF

# =====================================================
# 📄 activity_main.xml — MAY TATLONG BILOG NA PIHITAN!
# =====================================================
echo "📄 Gumagawa ng activity_main.xml..."
cat > app/src/main/res/layout/activity_main.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="20dp"
    android:background="#141418"
    android:gravity="center_horizontal"
    android:spacing="8dp">

    <!-- PAMAGAT -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="🎸 GITARA DISTORTION"
        android:textSize="26sp"
        android:textStyle="bold"
        android:textColor="#FF6622"
        android:layout_marginTop="8dp"
        android:layout_marginBottom="4dp"/>

    <!-- STATUS BAR -->
    <TextView
        android:id="@+id/statusText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="⚪ Isaksak iRig → Pihitan at Pindutin ON"
        android:textSize="15sp"
        android:gravity="center"
        android:padding="10dp"
        android:background="#222228"
        android:textColor="#888888"
        android:layout_marginBottom="16dp"/>

    <!-- TATLONG BILOG NA PIHITAN SA HILERA -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:spacing="8dp">

        <!-- 🔊 VOLUME -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center">

            <TextView
                android:id="@+id/volumeText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🔊 VOLUME: 50%"
                android:textSize="14sp"
                android:textColor="#FF9944"
                android:textStyle="bold"
                android:layout_marginBottom="4dp"/>

            <com.gitaradistortion.KnobView
                android:id="@+id/volKnob"
                android:layout_width="90dp"
                android:layout_height="90dp"/>
        </LinearLayout>

        <!-- 🎵 TONE -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center">

            <TextView
                android:id="@+id/toneText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🎵 TONE: 50%"
                android:textSize="14sp"
                android:textColor="#44DDAA"
                android:textStyle="bold"
                android:layout_marginBottom="4dp"/>

            <com.gitaradistortion.KnobView
                android:id="@+id/toneKnob"
                android:layout_width="90dp"
                android:layout_height="90dp"/>
        </LinearLayout>

        <!-- 💥 DISTORTION -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center">

            <TextView
                android:id="@+id/distortionText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="💥 DIST: 5.3x"
                android:textSize="14sp"
                android:textColor="#FF4444"
                android:textStyle="bold"
                android:layout_marginBottom="4dp"/>

            <com.gitaradistortion.KnobView
                android:id="@+id/distKnob"
                android:layout_width="90dp"
                android:layout_height="90dp"/>
        </LinearLayout>
    </LinearLayout>

    <Space
        android:layout_width="match_parent"
        android:layout_height="16dp"/>

    <!-- 🟢🔴 ON/OFF BUTTON -->
    <Button
        android:id="@+id/powerBtn"
        android:layout_width="match_parent"
        android:layout_height="65dp"
        android:text="🔴 TURN ON"
        android:textSize="22sp"
        android:textStyle="bold"
        android:backgroundColor="#228833"
        android:textColor="#FFFFFF"
        android:layout_marginHorizontal="10dp"/>

    <Space
        android:layout_width="match_parent"
        android:layout_height="10dp"/>

    <!-- 🚨 EMERGENCY OFF -->
    <Button
        android:id="@+id/emergencyBtn"
        android:layout_width="match_parent"
        android:layout_height="75dp"
        android:text="🚨 EMERGENCY OFF\nITIGIL AGAD!"
        android:textSize="19sp"
        android:textStyle="bold"
        android:backgroundColor="#CC1111"
        android:textColor="#FFFF66"
        android:layout_marginHorizontal="10dp"/>

</LinearLayout>
EOF

# =====================================================
# 📄 .github/workflows/build.yml
# =====================================================
echo "📄 Gumagawa ng GitHub Actions workflow..."
cat > .github/workflows/build.yml << 'EOF'
name: Build Gitara Distortion APK
on:
  push: { branches: [main, master] }
  workflow_dispatch: {}
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Setup Gradle
        run: |
          [ -f gradlew ] || (gradle wrapper --gradle-version=8.2 && chmod +x gradlew)
          chmod +x gradlew 2>/dev/null || true
      - name: Build APK
        run: ./gradlew assembleDebug --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: Gitara-Distortion-BILOG-NA-KNOB
          path: app/build/outputs/apk/debug/app-debug.apk
EOF

# =====================================================
# ✅ TAPOS NA!
# =====================================================
echo ""
echo "✅ ========================================="
echo "   BILOG NA PIHITAN — LAHAT GINAGAWA NA!"
echo "============================================"
echo ""
echo "🎨 ITSURA NG APP:"
echo "   🔊 VOLUME — KAHEL NA BILOG"
echo "   🎵 TONE   — BERDE NA BILOG"
echo "   💥 DIST   — PULANG BILOG"
echo ""
echo "👆 PAANO GAMITIN ANG BILOG:"
echo "   • Hawakan at I-ikot pakanan → PATAAS"
echo "   • Hawakan at I-ikot pakaliwa → PABABA"
echo ""
echo "🚀 SUSUNOD: Pindutin ang 'Run workflow' sa Actions tab!"
echo ""
