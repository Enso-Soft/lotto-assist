package com.enso.domain.model

enum class TicketSortType(val displayName: String) {
    REGISTERED_DATE_DESC("최신 등록순"),
    REGISTERED_DATE_ASC("오래된 등록순"),
    ROUND_DESC("최신 회차순"),
    ROUND_ASC("오래된 회차순");

    companion object {
        val DEFAULT = REGISTERED_DATE_DESC
    }
}
