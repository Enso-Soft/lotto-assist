package com.enso.lotto_assist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.network.api.LottoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val api: LottoApi
) : ViewModel() {
    fun getLotto() {
        viewModelScope.launch {
            val result = api.getLottoNumber(
                drwNo = 1
            )

            Log.d("whk__", "result : $result")
        }
    }
}