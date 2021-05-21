package fi.tuni.tamk.bottom.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to FoodApp! \n\n Start by pushing barcode icon to scan barcode"
    }
    val text: LiveData<String> = _text
}