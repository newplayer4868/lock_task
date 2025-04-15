package ui.preset

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cap.locktask.R
import viewmodel.PresetViewModel

class TimeSelectFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()

    private lateinit var durationInput: EditText
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_time_select, container, false)

        durationInput = view.findViewById(R.id.durationInput)
        nextButton = view.findViewById(R.id.nextButton)

        nextButton.setOnClickListener {
            val duration = durationInput.text.toString().trim()

            if (duration.isBlank()) {
                Toast.makeText(requireContext(), "시간을 입력해주세요 (예: 90분)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateField {
                it.copy(Time = duration)
            }

            val nextIndex = arguments?.getInt("nextIndex") ?: return@setOnClickListener
            (requireActivity() as PresetEditActivity).continueToNextFragment(nextIndex)
        }

        return view
    }
}
