package ui.preset

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cap.locktask.R
import viewmodel.PresetViewModel
import java.util.*

class PeriodSelectFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()

    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var nextButton: Button
    private lateinit var timeTextView: TextView

    private var startTime: String? = null
    private var endTime: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_period_select, container, false)

        startTimeButton = view.findViewById(R.id.startTimeButton)
        endTimeButton = view.findViewById(R.id.endTimeButton)
        nextButton = view.findViewById(R.id.nextButton)
        timeTextView = view.findViewById(R.id.timeTextView)

        startTimeButton.setOnClickListener {
            showTimePicker { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                updateTimeText()
            }
        }

        endTimeButton.setOnClickListener {
            showTimePicker { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                updateTimeText()
            }
        }

        nextButton.setOnClickListener {
            if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "시작 시간과 종료 시간을 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateField {
                it.copy(startTime = startTime, endTime = endTime)
            }

            val nextIndex = arguments?.getInt("nextIndex") ?: return@setOnClickListener
            (requireActivity() as PresetEditActivity).continueToNextFragment(nextIndex)
        }

        return view
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSet(hourOfDay, minute)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateTimeText() {
        val text = when {
            startTime != null && endTime != null -> "시작: $startTime  /  종료: $endTime"
            startTime != null -> "시작: $startTime"
            endTime != null -> "종료: $endTime"
            else -> ""
        }
        timeTextView.text = text
    }
}
