package com.gitaradistortion

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class PedalBoardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(6, 8, 6, 6)

        // ==========================================
        // ✅ TAAS: 🔴 MASTER ON/OFF — PAMATAY NG LAHAT!
        // ==========================================
        val masterBar = LinearLayout(ctx)
        masterBar.orientation = LinearLayout.HORIZONTAL
        masterBar.gravity = Gravity.CENTER
        masterBar.setBackgroundColor(0xFF220000.toInt())
        masterBar.setPadding(12, 8, 12, 8)

        val masterLabel = TextView(ctx)
        masterLabel.text = "🔴 MASTER"
        masterLabel.textSize = 16f
        masterLabel.setTextColor(0xFFFF4444.toInt())
        masterLabel.setPadding(0, 0, 16, 0)
        masterBar.addView(masterLabel)

        val masterBtn = Button(ctx)
        masterBtn.text = "🟢 ON"
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt())
        masterBtn.textSize = 13f
        masterBtn.setPadding(20, 4, 20, 4)
        masterBtn.minWidth = 120
        masterBtn.setOnClickListener {
            val isOn = masterBtn.text == "🟢 ON"
            MainActivity.setMasterEnabledGlobal(!isOn)
            if (isOn) {
                masterBtn.text = "⚪ OFF"
                masterBtn.setBackgroundColor(0xFF882222.toInt())
                masterLabel.setTextColor(0xFF888888.toInt())
            } else {
                masterBtn.text = "🟢 ON"
                masterBtn.setBackgroundColor(0xFF228833.toInt())
                masterLabel.setTextColor(0xFFFF4444.toInt())
            }
        }
        masterBar.addView(masterBtn)
        root.addView(masterBar)

        // ==========================================
        // ✅ ITAAS: TITLE
        // ==========================================
        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD"
        title.textSize = 18f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 8)
        root.addView(title)

        // ==========================================
        // ✅ GITNA: AKTIBONG PEDAL + CABINET SA KANAN!
        // ==========================================
        val mainRow = LinearLayout(ctx)
        mainRow.orientation = LinearLayout.HORIZONTAL
        mainRow.setPadding(2, 2, 2, 2)

        // ✅ KALIWA — AKTIBONG PEDAL
        val activeArea = LinearLayout(ctx)
        activeArea.orientation = LinearLayout.VERTICAL
        activeArea.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2.5f)
        activeArea.setPadding(2, 2, 6, 2)
        activeArea.setBackgroundColor(0xFF1A1A1A.toInt())

        val activeTitle = TextView(ctx)
        activeTitle.text = "✅ AKTIBONG PEDAL"
        activeTitle.textSize = 13f
        activeTitle.setTextColor(0xFF88FF88.toInt())
        activeTitle.gravity = Gravity.CENTER
        activeTitle.setPadding(0, 4, 0, 6)
        activeArea.addView(activeTitle)

        val pedalsRow = LinearLayout(ctx)
        pedalsRow.orientation = LinearLayout.HORIZONTAL
        pedalsRow.gravity = Gravity.CENTER

        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onThresholdChanged = { v -> MainActivity.setNoiseGateLevelGlobal(v) }
        pedalsRow.addView(noiseGate.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { v -> MainActivity.setVolumeLevelGlobal(v) }
        pedalsRow.addView(volume.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val gain = GainPedal()
        gain.onEnabledChanged = { MainActivity.setGainEnabledGlobal(it) }
        gain.onLevelChanged = { v -> MainActivity.setGainLevelGlobal(v) }
        pedalsRow.addView(gain.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        activeArea.addView(pedalsRow)
        mainRow.addView(activeArea)

        // ✅ KANAN — 📦 CABINET! HILAHIN MO PAPUNTA KALIWA!
        val cabinet = LinearLayout(ctx)
        cabinet.orientation = LinearLayout.VERTICAL
        cabinet.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        cabinet.setBackgroundColor(0xFF2A2A2A.toInt())
        cabinet.setPadding(6, 6, 6, 6)

        val cabTitle = TextView(ctx)
        cabTitle.text = "📦 CABINET"
        cabTitle.textSize = 13f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(0, 4, 0, 6)
        cabinet.addView(cabTitle)

        val cabHint = TextView(ctx)
        cabHint.text = "Hilahin pa-kaliwa"
        cabHint.textSize = 9f
        cabHint.setTextColor(0xFF777777.toInt())
        cabHint.gravity = Gravity.CENTER
        cabHint.setPadding(0, 0, 0, 6)
        cabinet.addView(cabHint)

        // ✅ MGA PEDAL NA NASA LOOB NG CABINET
        val cabItem1 = TextView(ctx)
        cabItem1.text = "🟠 OVERDRIVE"
        cabItem1.setTextColor(0xFFFFAA22.toInt())
        cabItem1.setBackgroundColor(0xFF383020.toInt())
        cabItem1.setPadding(8, 10, 8, 10)
        cabItem1.textSize = 12f
        cabItem1.gravity = Gravity.CENTER
        cabItem1.setMargins(0, 0, 0, 4)
        cabinet.addView(cabItem1)

        val cabItem2 = TextView(ctx)
        cabItem2.text = "🔴 DISTORTION"
        cabItem2.setTextColor(0xFFFF4422.toInt())
        cabItem2.setBackgroundColor(0xFF402020.toInt())
        cabItem2.setPadding(8, 10, 8, 10)
        cabItem2.textSize = 12f
        cabItem2.gravity = Gravity.CENTER
        cabItem2.setMargins(0, 0, 0, 4)
        cabinet.addView(cabItem2)

        val cabItem3 = TextView(ctx)
        cabItem3.text = "🎨 TONE"
        cabItem3.setTextColor(0xFF44DDAA.toInt())
        cabItem3.setBackgroundColor(0xFF203830.toInt())
        cabItem3.setPadding(8, 10, 8, 10)
        cabItem3.textSize = 12f
        cabItem3.gravity = Gravity.CENTER
        cabItem3.setMargins(0, 0, 0, 4)
        cabinet.addView(cabItem3)

        mainRow.addView(cabinet)
        root.addView(mainRow)
        return root
    }
}

// ✅ Tulong para maglagay ng espasyo
fun TextView.setMargins(l:Int=0, t:Int=0, r:Int=0, b:Int=0) {
    val lp = layoutParams as ViewGroup.MarginLayoutParams
    lp.setMargins(l,t,r,b)
    layoutParams = lp
}
