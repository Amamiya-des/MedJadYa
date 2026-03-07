package com.example.medjadya.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medjadya.data.model.RefillItemUi
import com.example.medjadya.data.api.ApiService
import com.example.medjadya.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RefillUiState(
    val items: List<RefillItemUi> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class RefillAlertViewModel(
    private val api: ApiService = ApiClient.api
) : ViewModel() {

    private val _state = MutableStateFlow(RefillUiState())
    val state: StateFlow<RefillUiState> = _state.asStateFlow()

    fun loadRefillAlerts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val data = api.getRefillAlerts()
                _state.value = RefillUiState(items = data, loading = false, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "โหลดข้อมูลไม่สำเร็จ"
                )
            }
        }
    }

    fun refillMedicine(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                api.refillMedicine(id)

                _state.value = _state.value.copy(
                    items = _state.value.items.filterNot { it.id == id }
                )

                onSuccess()

            } catch (e: Exception) {
                onError(e.message ?: "เติมยาไม่สำเร็จ")
            }
        }
    }
}
