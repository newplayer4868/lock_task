package ui

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.cap.locktask.R
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import model.TemporaryLockState

//TempLockSettingDialogFragment().show(parentFragmentManager, "TempLockSettingDialog")
class TempLockSettingDialogFragment : DialogFragment() {

    private var lockMinutes = 10
    private var cntul=3
    private var ONOFF=false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_temp_lock_setting, null)
        dialog.setContentView(view)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val timeText = view.findViewById<TextView>(R.id.lockTimeText)
        val cntulText = view.findViewById<TextView>(R.id.lockcntulText)
        val plusButton = view.findViewById<Button>(R.id.plusButton)
        val minusButton = view.findViewById<Button>(R.id.minusButton)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val cntulplusButton = view.findViewById<Button>(R.id.cntulplusButton)
        val cntulminusButton = view.findViewById<Button>(R.id.cntulminusButton)
        val onoffText=view.findViewById<TextView>(R.id.ONOFF )
        val ONBtn=view.findViewById<Button>(R.id.ON)
        val OFFBtn=view.findViewById<Button>(R.id.OFF)

        updateTimeText(timeText)
        updatecntulText(cntulText)
        updateONOFFText(onoffText) // 🔥 추가!


        plusButton.setOnClickListener {
            lockMinutes+=10
            updateTimeText(timeText)
        }

        minusButton.setOnClickListener {
            if (lockMinutes > 10) {
                lockMinutes-=10
                updateTimeText(timeText)
            }
        }
        cntulplusButton.setOnClickListener{
            cntul++
            updatecntulText(cntulText)
        }
        cntulminusButton.setOnClickListener{
            if (cntul > 1) {
                cntul--
                updatecntulText(cntulText)
            }
        }
        confirmButton.setOnClickListener {
            TemporaryLockState.lockStartTime = lockMinutes.toLong()
            TemporaryLockState.cntul = cntul
            dismiss()
        }
        ONBtn.setOnClickListener{
            TemporaryLockState.nopecall=true
            updateONOFFText(onoffText)
        }
        OFFBtn.setOnClickListener{
            TemporaryLockState.nopecall=false
            updateONOFFText(onoffText)
        }
        return dialog
    }
    private fun updateONOFFText(textView: TextView) {
        if (TemporaryLockState.nopecall) {
            textView.text = "전화 수신: ON"
        } else {
            textView.text = "전화 수신: OFF"
        }
    }

    private fun updateTimeText(textView: TextView) {
        textView.text = "잠금 시간: ${lockMinutes}분"
    }
    private fun updatecntulText(textView: TextView) {
        textView.text = "해제 횟수: ${cntul}회"
    }
}
