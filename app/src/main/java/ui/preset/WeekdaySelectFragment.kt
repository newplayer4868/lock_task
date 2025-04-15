package ui.preset

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cap.locktask.R
import viewmodel.PresetViewModel

class WeekdaySelectFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()

    private lateinit var weekdayLayout: LinearLayout
    private lateinit var nextButton: Button
    private val selectedDays = mutableSetOf<String>()

    private val weekdays = listOf("월", "화", "수", "목", "금", "토", "일")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_weekday_select, container, false)

        weekdayLayout = view.findViewById(R.id.weekdayLayout)
        nextButton = view.findViewById(R.id.nextButton)

        weekdays.forEach { day ->
            val checkBox = CheckBox(requireContext()).apply {
                text = day
                textSize = 16f
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDays.add(day)
                    } else {
                        selectedDays.remove(day)
                    }
                }
            }
            weekdayLayout.addView(checkBox)
        }

        nextButton.setOnClickListener {
            if (selectedDays.isEmpty()) {
                Toast.makeText(requireContext(), "하나 이상의 요일을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateField {
                it.copy(week = selectedDays.toList())
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SummaryFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
