package viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.Preset

class PresetViewModel : ViewModel() {

    private val _preset = MutableLiveData(Preset(name = "", lockType = ""))
    val preset: LiveData<Preset> get() = _preset

    fun updateField(update: (Preset) -> Preset) {
        _preset.value = _preset.value?.let(update)
    }


    fun resetPreset() {
        _preset.value = Preset(name = "", lockType = "")
    }
}
