package com.enso.lotto_assist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.network.api.LottoApi
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val api: LottoApi
) : ViewModel() {
    fun getLotto() {
        viewModelScope.launch {
            api.getLottoNumber(
                drwNo = LottoDate.getCurrentDrawNumber()
            )
        }
    }
}