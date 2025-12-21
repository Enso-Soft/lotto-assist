package com.enso.domain.model

data class SyncResult(
    val successCount: Int,
    val failedCount: Int,
    val totalCount: Int
) {
    val isFullSuccess: Boolean get() = failedCount == 0
    val isPartialSuccess: Boolean get() = successCount > 0 && failedCount > 0
    val isFullFailure: Boolean get() = successCount == 0 && totalCount > 0
}
