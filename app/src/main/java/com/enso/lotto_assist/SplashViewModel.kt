package com.enso.lotto_assist

import android.util.Log
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
            Log.d("whk__", "최신 추첨 회자 : ${LottoDate.getCurrentDrawNumber()}")
            Log.d("whk__", "추첨 날짜 : ${LottoDate.getNextDrawDateTime().time}")


            val result = api.getLottoNumber(
                drwNo = LottoDate.getCurrentDrawNumber()
            )

            Log.d("whk__", "result : ${result}")
        }
    }
}