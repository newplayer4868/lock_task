package ui.preset

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.cap.locktask.R
import com.cap.locktask.screen.LocationRangeSettingActivity
import viewmodel.PresetViewModel

class LocationSelectFragment : Fragment() {

    private val viewModel: PresetViewModel by activityViewModels()

    private lateinit var locationResult: TextView
    private lateinit var openMapButton: Button
    private lateinit var nextButton: Button

    private var lat: Double? = null
    private var lng: Double? = null
    private var radius: Int? = null

    private val locationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                lat = data?.getDoubleExtra("latitude", 0.0)
                lng = data?.getDoubleExtra("longitude", 0.0)
                radius = data?.getIntExtra("radius", 0)

                if (lat != null && lng != null && radius != null) {
                    locationResult.text = "위치: (${lat}, ${lng}) / 반경: ${radius}m"
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.c_fragment_location_select, container, false)

        locationResult = view.findViewById(R.id.locationResult)
        openMapButton = view.findViewById(R.id.openMapButton)
        nextButton = view.findViewById(R.id.nextButton)

        openMapButton.setOnClickListener {
            val intent = Intent(requireContext(), LocationRangeSettingActivity::class.java)
            locationLauncher.launch(intent)
        }

        nextButton.setOnClickListener {
            if (lat == null || lng == null || radius == null) {
                Toast.makeText(requireContext(), "위치를 먼저 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateField {
                it.copy(latitude = lat, longitude = lng, radius = radius)
            }


            val nextIndex = arguments?.getInt("nextIndex") ?: return@setOnClickListener
            (requireActivity() as PresetEditActivity).continueToNextFragment(nextIndex)
        }

        return view
    }
}
