package ui.preset


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cap.locktask.R
import model.Preset


class ButtonInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.c_activity_button_info)

        val infoTextView: TextView = findViewById(R.id.infoTextView)

        // 인텐트에서 프리셋 가져오기
        val preset: Preset? = intent.getParcelableExtra("preset")

        if (preset != null) {
            displayPresetInfo(preset, infoTextView)
        } else {
            infoTextView.text = "프리셋 정보를 불러올 수 없습니다."
        }
    }


    private fun displayPresetInfo(preset: Preset, infoTextView: TextView) {
        // 프리셋 정보 가져오기
        val name = preset.name
        val lockType = preset.lockType ?: "설정되지 않음"
        val latitude = preset.latitude?.toString() ?: "설정되지 않음"
        val longitude = preset.longitude?.toString() ?: "설정되지 않음"
        val radius = preset.radius?.toString() ?: "설정되지 않음"
        val startTime = preset.startTime ?: "설정되지 않음"
        val endTime = preset.endTime ?: "설정되지 않음"
        val selectedApps = preset.selectedApps?.joinToString(", ") ?: "설정되지 않음"
        val isactive = if (preset.isactivity == true) "Active" else "Inactive"


        // 프리셋 정보 화면에 표시
        infoTextView.text = """
            프리셋 이름: $name
            잠금 형태: $lockType
            시작 시간: $startTime
            종료 시간: $endTime
            좌표: ($latitude, $longitude)
            반경: $radius
            설정된 앱: $selectedApps
            활성화 :$isactive
        """.trimIndent()
    }

}
