package org.sitebay.android.imageeditor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.sitebay.android.imageeditor.crop.CropViewModel.CropResult
import org.sitebay.android.imageeditor.viewmodel.Event

class EditImageViewModel : ViewModel() {
    private val _cropResult = MutableLiveData<Event<CropResult>>()
    val cropResult: LiveData<Event<CropResult>> = _cropResult

    fun setCropResult(cropResult: CropResult) {
        _cropResult.value = Event(cropResult)
    }
}
