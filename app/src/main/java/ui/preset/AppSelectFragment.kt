package ui.preset

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cap.locktask.R
import viewmodel.PresetViewModel

class AppSelectFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()
    private lateinit var appListLayout: LinearLayout
    private lateinit var nextButton: Button
    private val selectedApps = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_app_select, container, false)

        appListLayout = view.findViewById(R.id.appListLayout)
        nextButton = view.findViewById(R.id.nextButton)

        loadInstalledApps()

        nextButton.setOnClickListener {
            if (selectedApps.isEmpty()) {
                Toast.makeText(requireContext(), "하나 이상의 앱을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 선택한 앱 저장
            viewModel.updateField {
                it.copy(selectedApps = selectedApps.toList())
            }

            val nextIndex = arguments?.getInt("nextIndex") ?: return@setOnClickListener
            (requireActivity() as PresetEditActivity).continueToNextFragment(nextIndex)
        }

        return view
    }

    private fun loadInstalledApps() {
        val pm = requireContext().packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { pm.getApplicationLabel(it).toString() }

        apps.forEach { app ->
            val checkBox = CheckBox(requireContext()).apply {
                val appName = pm.getApplicationLabel(app).toString()
                val appIcon = pm.getApplicationIcon(app)

                text = appName
                setCompoundDrawablesWithIntrinsicBounds(appIcon, null, null, null)
                compoundDrawablePadding = 16

                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (selectedApps.size >= 2) {
                            buttonView.isChecked = false // ✅ 체크 해제
                            Toast.makeText(context, "최대 2개까지만 선택할 수 있어요.", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedApps.add(app.packageName)
                        }
                    } else {
                        selectedApps.remove(app.packageName)
                    }
                }


                setPadding(12, 12, 12, 12)
                textSize = 16f
            }

            appListLayout.addView(checkBox)
        }
    }
}
