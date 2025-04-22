package ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cap.locktask.R
import com.cap.locktask.screen.ImageSelectActivity
import com.cap.locktask.screen.MusicManagerActivity
import com.cap.locktask.utils.SharedPreferencesUtils
import service.LockScreenService
import java.io.File

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.b_fragment_home, container, false)

        // 잠금 버튼 클릭 이벤트
        view.findViewById<View>(R.id.lockButton).setOnClickListener {
            Log.d("HomeFragment", "\uD83D\uDD12 lockButton 클릭된다")
            startLockScreenService()
        }

        // 🎵 음악 선택 버튼 클릭 이벤트
        view.findViewById<Button>(R.id.musicButton).setOnClickListener {
            selectMusicActivity()
        }

        view.findViewById<Button>(R.id.imageButton).setOnClickListener {
            selectImageActivity()
        }

        return view
    }

    private fun startLockScreenService() {
        val intent = Intent(requireContext(), LockScreenService::class.java)
        requireContext().startService(intent)
    }

    private fun selectImageActivity() {
        val intent = Intent(requireContext(), ImageSelectActivity::class.java)
        startActivity(intent)
    }

    private fun selectMusicActivity() {
        val context = requireContext()
        val musicFiles = SharedPreferencesUtils.getSavedMusicFiles(context)

        musicFiles.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                // 파일 존재 확인 로그
            } else {
                // 파일 없음 로그
            }
        }
        val intent = Intent(requireContext(), MusicManagerActivity::class.java)
        startActivity(intent)
    }
}