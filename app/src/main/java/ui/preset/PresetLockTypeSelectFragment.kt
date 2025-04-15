package ui.preset

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.cap.locktask.R

class PresetLockTypeSelectFragment : Fragment() {

    private val lockTypes = listOf(
        "목적지 잠금 해제",
        "목적지 잠금",
        "기간 잠금",
        "어플 사용량 잠금",
        "어플 할당량 잠금"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_preset_lock_type_select, container, false)

        val listView = view.findViewById<ListView>(R.id.lockTypeListView)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, lockTypes)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedType = lockTypes[position]
            (requireActivity() as PresetEditActivity).startPresetFlow(selectedType)
        }

        return view
    }
}
