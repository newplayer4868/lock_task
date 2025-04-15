
package ui.page1


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

import com.cap.locktask.R
import model.Preset
import ui.preset.PresetEditActivity
import com.cap.locktask.utils.SharedPreferencesUtils
import ui.preset.ButtonInfoActivity


class Page1Fragment : Fragment(R.layout.b_fragment_page1) {

    private lateinit var containerLayout: LinearLayout
    private lateinit var addButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var selectAllButton: Button
    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.b_fragment_page1, container, false)

        containerLayout = rootView.findViewById(R.id.container)
        addButton = rootView.findViewById(R.id.addButton)
        editButton = rootView.findViewById(R.id.editButton)
        deleteButton = rootView.findViewById(R.id.deleteButton)
        selectAllButton = rootView.findViewById(R.id.selectAllButton)

        deleteButton.visibility = View.GONE
        selectAllButton.visibility = View.GONE

        setupButtons()
        loadGeneratedButtons()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        loadGeneratedButtons()
    }

    private fun setupButtons() {
        addButton.setOnClickListener { createNewPreset() }
        editButton.setOnClickListener { toggleEditMode() }
        deleteButton.setOnClickListener { deleteSelectedButtons() }
        selectAllButton.setOnClickListener { toggleSelectAll() }
    }

    private fun createNewPreset() {
        if (!isEditMode) {
            val intent = Intent(requireContext(), PresetEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        addButton.isEnabled = !isEditMode
        deleteButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
        selectAllButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
        editButton.text = if (isEditMode) "완료" else "편집"
        updateCheckboxVisibility()

        // 🔍 편집 모드 진입 시 SharedPreferences 내용 로그 출력
        if (isEditMode) {
            val prefs = requireContext().getSharedPreferences("ButtonPrefs", Context.MODE_PRIVATE)
            val count = prefs.getInt("buttonCount", 0)
            //Log.d("PresetDebug", "현재 저장된 프리셋 개수: $count")

            for (i in 1..count) {
                val name = prefs.getString("button_${i}_name", null)
                //Log.d("PresetDebug", "[$i] 이름: $name")
            }
        }
    }


    private fun updateCheckboxVisibility() {
        for (i in 0 until containerLayout.childCount) {
            val layout = containerLayout.getChildAt(i) as LinearLayout
            val checkBox = layout.getChildAt(1) as CheckBox
            checkBox.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }
    }

    private fun toggleSelectAll() {
        val selectAll = selectAllButton.text == "전체 선택"
        selectAllButton.text = if (selectAll) "전체 선택 해제" else "전체 선택"

        for (i in 0 until containerLayout.childCount) {
            val layout = containerLayout.getChildAt(i) as LinearLayout
            val checkBox = layout.getChildAt(1) as CheckBox
            checkBox.isChecked = selectAll
        }
    }

    private fun updateSelectAllButtonState() {
        val totalCheckboxes = containerLayout.childCount
        val checkedCount = (0 until totalCheckboxes).count { i ->
            val layout = containerLayout.getChildAt(i) as LinearLayout
            val checkBox = layout.getChildAt(1) as CheckBox
            checkBox.isChecked
        }

        selectAllButton.text = if (checkedCount == totalCheckboxes) {
            "전체 선택 해제"
        } else {
            "전체 선택"
        }
    }

    private fun loadGeneratedButtons() {
        val sharedPreferences = requireContext().getSharedPreferences("ButtonPrefs", Context.MODE_PRIVATE)
        val buttonCount = sharedPreferences.getInt("buttonCount", 0)

        containerLayout.removeAllViews()

        for (i in 1..buttonCount) {
            val buttonName = sharedPreferences.getString("button_${i}_name", "Button $i") ?: "Button $i"
            val preset = SharedPreferencesUtils.loadPreset(requireContext(), buttonName)
            if (preset != null) {
                val buttonWithCheckBox = createButtonWithCheckBox(preset, i)
                containerLayout.addView(buttonWithCheckBox)
            }
        }
    }
    private fun createButtonWithCheckBox(preset: Preset, id: Int): LinearLayout {
        val context = requireContext()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        val button = Button(context).apply {
            text = preset.name
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val intent = Intent(context, ButtonInfoActivity::class.java)
                intent.putExtra("preset", preset)
                startActivity(intent)
            }
        }

        val checkBox = CheckBox(context).apply {
            visibility = if (isEditMode) View.VISIBLE else View.GONE
            tag = preset.name // ← 여기에 이름 저장
            setOnCheckedChangeListener { _, _ -> updateSelectAllButtonState() }
        }


        val switch = Switch(context).apply {

            isChecked = preset.isactivity
            text = if (isChecked) "활성화" else "비활성화"
            setOnCheckedChangeListener { _, isChecked ->
                text = if (isChecked) "활성화" else "비활성화"

                val updatedPreset = preset.copy(isactivity = isChecked)
                SharedPreferencesUtils.savePreset(context, updatedPreset.name, updatedPreset)
                Log.d("PresetDebug", "활성화 상태 저장됨: ${updatedPreset.name} = ${updatedPreset.isactivity}")

            }
        }

        layout.addView(button)
        layout.addView(checkBox)
        layout.addView(switch)

        return layout
    }


    private fun deleteSelectedButtons() {
        val prefs = requireContext().getSharedPreferences("ButtonPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val remainingNames = mutableListOf<String>()
        val deletedNames = mutableListOf<String>()

        for (i in 0 until containerLayout.childCount) {
            val layout = containerLayout.getChildAt(i) as? LinearLayout ?: continue
            val button = layout.getChildAt(0) as? Button ?: continue
            val checkBox = layout.getChildAt(1) as? CheckBox ?: continue

            val buttonName = button.text.toString()

            if (checkBox.isChecked) {
                deletedNames.add(buttonName)
            } else {
                remainingNames.add(buttonName)
            }
        }

        val fullEditor = prefs.edit()
        deletedNames.forEach { name ->
            fullEditor.remove("preset_$name")
            //Log.d("PresetDebug", "삭제된 preset: $name")
        }

        for (i in 1..prefs.getInt("buttonCount", 0)) {
            fullEditor.remove("button_${i}_name")
        }

        remainingNames.forEachIndexed { index, name ->
            fullEditor.putString("button_${index + 1}_name", name)
        }
        fullEditor.putInt("buttonCount", remainingNames.size)

        fullEditor.apply()

        loadGeneratedButtons()

    }

}
