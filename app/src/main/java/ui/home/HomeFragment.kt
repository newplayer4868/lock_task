package ui.home


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
            Log.d("HomeFragment", "🔒 lockButton 클릭됨")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val hasOverlayPermission = Settings.canDrawOverlays(requireContext())
                //Log.d("HomeFragment", "오버레이 권한 있음?: $hasOverlayPermission")
                if (!Settings.canDrawOverlays(requireContext())) {

                    Toast.makeText(requireContext(), "화면 잠금을 위한 오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
                } else {startLockScreenService()}
            } else {
                //Log.d("HomeFragment", "SDK < M, 권한 체크 없이 서비스 실행")
                startLockScreenService()}
        }

         //🎵 음악 선택 버튼 클릭 이벤트 추가
        view.findViewById<Button>(R.id.musicButton).setOnClickListener {
            selectMusicActivity()
}
        view.findViewById<Button>(R.id.imageButton).setOnClickListener{
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
        startActivity(intent) // 화면 전환
    }

    private fun selectMusicActivity() {
        val context = requireContext()
        val musicFiles = SharedPreferencesUtils.getSavedMusicFiles(context)

        //Log.d("HomeFragment", "🎵 저장된 음원 수: ${musicFiles.size}")
        musicFiles.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                //Log.d("HomeFragment", "음원 - 이름: ${file.name}, 경로: ${file.absolutePath}")
            } else {
                //Log.w("HomeFragment", "경로는 있지만 파일 없음: $path")
            }
        }
        val intent = Intent(requireContext(),MusicManagerActivity::class.java)
        startActivity(intent) // 화면 전환
     }
    companion object {
        private const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }
}
