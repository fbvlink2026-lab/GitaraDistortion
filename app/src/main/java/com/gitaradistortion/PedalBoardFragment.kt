package com.gitaradistortion

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class PedalBoardFragment : Fragment() {
    private var cabinetVisible = false
    private lateinit var mainRow: LinearLayout
    private lateinit var activeArea: LinearLayout
    private lateinit var cabinetArea: LinearLayout
    private lateinit var cabinetInner: LinearLayout
    private lateinit var pedalRowsContainer: LinearLayout
    private var startX = 0f

    private val activePedals = mutableSetOf<String>()
    private val initialPedals = listOf("noisegate", "volume", "gain")
    private val allCabinetPedals = listOf("overdrive", "distortion", "tone")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(6, 8, 6, 6)

        // ✅ MASTER ON/OFF
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

        // ✅ TITLE
        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD ← Hilahin pakaliwa"
        title.textSize = 13f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 6, 0, 6)
        root.addView(title)

        // ✅ HILAHAN
        mainRow = LinearLayout(ctx)
        mainRow.orientation = LinearLayout.HORIZONTAL
        mainRow.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startX = event.rawX
                MotionEvent.ACTION_MOVE -> {
                    val delta = startX - event.rawX
                    if (delta > 80 && !cabinetVisible) showCabinet()
                    if (delta < -80 && cabinetVisible) hideCabinet()
                }
            }
            true
        }

        // ✅ KALIWA — AKTIBONG PEDAL
        activeArea = LinearLayout(ctx)
        activeArea.orientation = LinearLayout.VERTICAL
        val activeLP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        activeArea.layoutParams = activeLP
        activeArea.setPadding(2, 2, 6, 2)
        activeArea.setBackgroundColor(0xFF1A1A1A.toInt())

        val activeTitle = TextView(ctx)
        activeTitle.text = "✅ AKTIBONG PEDAL (3/hanay)"
        activeTitle.textSize = 12f
        activeTitle.setTextColor(0xFF88FF88.toInt())
        activeTitle.gravity = Gravity.CENTER
        activeTitle.setPadding(0, 4, 0, 6)
        activeArea.addView(activeTitle)

        pedalRowsContainer = LinearLayout(ctx)
        pedalRowsContainer.orientation = LinearLayout.VERTICAL
        activeArea.addView(pedalRowsContainer)
        mainRow.addView(activeArea)

        // ✅ KANAN — CABINET
        cabinetArea = LinearLayout(ctx)
        cabinetArea.orientation = LinearLayout.VERTICAL
        val cabLP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.layoutParams = cabLP
        cabinetArea.setBackgroundColor(0xFF2A2A2A.toInt())
        cabinetArea.setPadding(6, 6, 6, 6)
        cabinetArea.visibility = View.GONE

        val cabTitle = TextView(ctx)
        cabTitle.text = "📦 CABINET — Pindutin → Ilipat / Pindutin muli → Ibalik"
        cabTitle.textSize = 10f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(0, 4, 0, 8)
        cabinetArea.addView(cabTitle)

        val cabScroll = ScrollView(ctx)
        cabinetInner = LinearLayout(ctx)
        cabinetInner.orientation = LinearLayout.VERTICAL

        addCabinetPedal(ctx, "overdrive", "🟠 OVERDRIVE", 0xFFFFAA22.toInt())
        addCabinetPedal(ctx, "distortion", "🔴 DISTORTION", 0xFFFF4422.toInt())
        addCabinetPedal(ctx, "tone", "🎨 TONE", 0xFF44DDAA.toInt())

        cabScroll.addView(cabinetInner)
        cabinetArea.addView(cabScroll)
        mainRow.addView(cabinetArea)
        root.addView(mainRow)

        // ✅ SIMULA — MAY 3 NA AGAD
        activePedals.addAll(initialPedals)
        updatePedalLayout()
        return root
    }

    private fun addCabinetPedal(ctx: android.content.Context, id: String, label: String, color: Int) {
        val tv = TextView(ctx)
        tv.text = label
        tv.setTextColor(color)
        tv.setBackgroundColor(0xFF333333.toInt())
        tv.setPadding(12, 14, 12, 14)
        tv.textSize = 13f
        tv.gravity = Gravity.CENTER
        tv.tag = id
        tv.setOnClickListener { togglePedal(id) }
        cabinetInner.addView(tv)
    }

    private fun togglePedal(pedalId: String) {
        val ctx = context ?: return
        val btn = cabinetInner.findViewWithTag<TextView>(pedalId) ?: return

        if (activePedals.contains(pedalId)) {
            activePedals.remove(pedalId)
            btn.setTextColor(getPedalColor(pedalId))
            btn.setBackgroundColor(0xFF333333.toInt())
        } else {
            activePedals.add(pedalId)
            btn.setTextColor(0xFF777777.toInt())
            btn.setBackgroundColor(0xFF111111.toInt())
        }
        updatePedalLayout()
    }

    private fun getPedalColor(pedalId: String): Int {
        return when(pedalId) {
            "overdrive" -> 0xFFFFAA22.toInt()
            "distortion" -> 0xFFFF4422.toInt()
            "tone" -> 0xFF44DDAA.toInt()
            else -> 0xFFFFFFFF.toInt()
        }
    }

    private fun updatePedalLayout() {
        val ctx = context ?: return
        pedalRowsContainer.removeAllViews()

        val list = activePedals.toList()
        for (i in list.indices step 3) {
            val row = LinearLayout(ctx)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER
            row.setPadding(2, 4, 2, 4)

            for (j in i until minOf(i + 3, list.size)) {
                val view = createPedalView(list[j], ctx)
                row.addView(view, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            pedalRowsContainer.addView(row)
        }
    }

    private fun createPedalView(pedalId: String, ctx: android.content.Context): View {
        return when (pedalId) {
            "noisegate" -> NoiseGatePedal().makeView(ctx)
            "volume" -> VolumePedal().makeView(ctx)
            "gain" -> GainPedal().makeView(ctx)
            "overdrive" -> OverdrivePedal().makeView(ctx)
            "distortion" -> DistortionPedal().makeView(ctx)
            "tone" -> TonePedal().makeView(ctx)
            else -> TextView(ctx).apply { text = pedalId }
        }
    }

    private fun showCabinet() {
        cabinetVisible = true
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.visibility = View.VISIBLE
    }

    private fun hideCabinet() {
        cabinetVisible = false
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.visibility = View.GONE
    }
}
