package ui.home

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.cap.locktask.R
import com.cap.locktask.screen.ImageSelectActivity
import com.cap.locktask.screen.MusicManagerActivity
import com.cap.locktask.utils.SharedPreferencesUtils
import model.ClockNeedleView
import model.TemporaryLockState
import service.LockScreenService
import service.TemporaryLockService
import ui.PopupDialogFragment
import ui.TempLockSettingDialogFragment
import java.io.File

class HomeFragment:Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?):View?
    {
        val view = inflater.inflate(R.layout.b_fragment_home, container, false)
        view.findViewById<Button>(R.id.musicButton).setOnClickListener{selectMusicActivity()}
        view.findViewById<Button>(R.id.imageButton).setOnClickListener{selectImageActivity()}
        view.findViewById<Button>(R.id.bButton).setOnClickListener{TempLockSettingDialogFragment().show(parentFragmentManager, "TempLockSettingDialog")}
        view.findViewById<Button>(R.id.aButton).setOnClickListener {
            val startButton = view.findViewById<Button>(R.id.aButton)
            val settingButton = view.findViewById<Button>(R.id.bButton)

            val lockMinutes = TemporaryLockState.lockStartTime
            val cntulMinutes = TemporaryLockState.cntul

            if (lockMinutes != null && lockMinutes > 0) {
                // 🔒 버튼 비활성화
                startButton.isEnabled = false
                settingButton.isEnabled = false
                startButton.alpha = 0.5f
                settingButton.alpha = 0.5f

                // 🔐 상태 변경
                TemporaryLockState.ison = true

                val intent = Intent(requireContext(), TemporaryLockService::class.java).apply {
                    putExtra("LOCK_MINUTES", lockMinutes.toInt())
                    putExtra("CNTUL_MINUTES", cntulMinutes)
                }
                requireContext().startForegroundService(intent)
            }
        }


        return view
    }
    private fun selectImageActivity(){startActivity(Intent(requireContext(), ImageSelectActivity ::class.java))}
    private fun selectMusicActivity(){startActivity(Intent(requireContext(), MusicManagerActivity::class.java))}
}