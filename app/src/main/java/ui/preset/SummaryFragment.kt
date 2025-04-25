package ui.preset

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cap.locktask.R
import model.Preset
import com.cap.locktask.utils.SharedPreferencesUtils
import com.cap.locktask.worker.PresetCheckWorker
import viewmodel.PresetViewModel

class SummaryFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()
    private lateinit var nameInput: EditText
    private lateinit var DescriptionInput: EditText
    private lateinit var lockTypeText: TextView
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_summary, container, false)

        DescriptionInput=view.findViewById(R.id.descriptionInput)
        nameInput = view.findViewById(R.id.nameInput)
        lockTypeText = view.findViewById(R.id.lockTypeText)
        saveButton = view.findViewById(R.id.saveButton)

        viewModel.preset.observe(viewLifecycleOwner) { preset ->
            lockTypeText.text = "락타입: ${preset.lockType}"
            nameInput.setText(preset.name)
            DescriptionInput.setText((preset.description))
        }

        saveButton.setOnClickListener {
            val description=DescriptionInput.text.toString()
            val name = nameInput.text.toString()
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "프리셋 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (SharedPreferencesUtils.isPresetNameExists(requireContext(), name)) {
                Toast.makeText(requireContext(), "이미 존재하는 프리셋 이름입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateField {
                it.copy(
                    name = name,
                    description = description
                )
            }

            val updatedPreset = viewModel.preset.value!!

            if (!isPresetValid(updatedPreset)) {
                Toast.makeText(requireContext(), "설정이 부족합니다. 필수 항목을 확인해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SharedPreferencesUtils.savePreset(requireContext(), updatedPreset.name, updatedPreset)
            Toast.makeText(requireContext(), "프리셋 저장 완료!", Toast.LENGTH_SHORT).show()

            val request = OneTimeWorkRequestBuilder<PresetCheckWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(request)


            Log.d("AlarmReceiver", "✅ PresetCheckWorker 실행 요청 완료")
            requireActivity().finish()
        }

        return view
    }

    private fun isPresetValid(preset: Preset): Boolean {
        return when (preset.lockType) {
            "목적지 잠금 해제" -> {
                preset.latitude != null &&
                        preset.longitude != null &&
                        preset.radius != null &&
                        !preset.Time.isNullOrEmpty() &&
                        !preset.startTime.isNullOrEmpty() &&
                        !preset.endTime.isNullOrEmpty() &&
                        !preset.week.isNullOrEmpty()
            }

            "목적지 잠금" -> {
                preset.latitude != null &&
                        preset.longitude != null &&
                        preset.radius != null &&
                        !preset.week.isNullOrEmpty()
            }


            "기간 잠금" -> {
                !preset.startTime.isNullOrEmpty() &&
                        !preset.endTime.isNullOrEmpty() &&
                        !preset.week.isNullOrEmpty()
            }

            "어플 사용량 잠금" -> {
                !preset.selectedApps.isNullOrEmpty() &&
                        !preset.Time.isNullOrEmpty() &&
                        !preset.week.isNullOrEmpty()
            }

            "어플 할당량 잠금" -> {
                !preset.selectedApps.isNullOrEmpty() &&
                        !preset.Time.isNullOrEmpty() &&
                        !preset.week.isNullOrEmpty()
            }

            else -> false
        }
    }
}
