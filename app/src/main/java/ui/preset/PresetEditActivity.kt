package ui.preset
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cap.locktask.R

import viewmodel.PresetViewModel

class PresetEditActivity : AppCompatActivity() {

    private val viewModel: PresetViewModel by viewModels()
    private lateinit var fragmentSequence: List<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.c_activity_preset_edit)

        // 프리셋 초기화
        viewModel.resetPreset()

        // 시작은 락타입 선택 화면
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PresetLockTypeSelectFragment())
            .commit()
    }

    fun startPresetFlow(lockType: String) {
        viewModel.updateField { it.copy(lockType = lockType) }

        fragmentSequence = getFragmentSequenceForLockType(lockType)

        // 첫 프래그먼트부터 실행
        if (fragmentSequence.isNotEmpty()) {
            val fragment = fragmentSequence[0]
            fragment.arguments = Bundle().apply { putInt("nextIndex", 1) }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    fun continueToNextFragment(index: Int) {
        if (index < fragmentSequence.size) {
            val fragment = fragmentSequence[index]
            fragment.arguments = Bundle().apply { putInt("nextIndex", index + 1) }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getFragmentSequenceForLockType(lockType: String): List<Fragment> {
        val list = mutableListOf<Fragment>()

        when (lockType) {
            "목적지 잠금 해제" -> {
                list += OptionAppSelectFragment()
                list += LocationSelectFragment()
                list += PeriodSelectFragment()
                list += TimeSelectFragment()
                list += WeekdaySelectFragment()
            }

            "목적지 잠금" -> {

                list += OptionAppSelectFragment()
                list += LocationSelectFragment()
                list += WeekdaySelectFragment()
            }


            "기간 잠금" -> {

                list += OptionAppSelectFragment()
                list += PeriodSelectFragment()
                list += WeekdaySelectFragment()
            }

            "어플 사용량 잠금" -> {

                list += AppSelectFragment()
                list += TimeSelectFragment()
                list += WeekdaySelectFragment()

            }

            "어플 할당량 잠금" -> {
                list += AppSelectFragment()
                list += TimeSelectFragment()
                list += WeekdaySelectFragment()
            }
        }

        list += SummaryFragment()
        return list
    }
}
