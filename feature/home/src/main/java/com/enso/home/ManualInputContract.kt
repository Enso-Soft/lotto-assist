package com.enso.home

import java.util.UUID

/**
 * 수동 입력 게임 데이터 모델
 *
 * @property id 게임 식별자 (A, B, C, D, E)
 * @property numbers 선택된 번호 목록 (최대 6개)
 * @property isAuto 자동/수동 구분
 */
data class ManualGame(
    val id: String,
    val numbers: List<Int> = emptyList(),
    val isAuto: Boolean = false
) {
    /**
     * 게임 완료 여부 (6개 번호 선택됨)
     */
    val isComplete: Boolean
        get() = numbers.size == 6

    /**
     * 게임 진행 중 여부 (1~5개 번호 선택됨)
     */
    val isInProgress: Boolean
        get() = numbers.isNotEmpty() && numbers.size < 6

    /**
     * 빈 게임 여부 (0개 번호)
     */
    val isEmpty: Boolean
        get() = numbers.isEmpty()

    companion object {
        private val GAME_IDS = listOf("A", "B", "C", "D", "E")

        /**
         * 새 게임 생성
         */
        fun create(index: Int, isAuto: Boolean = false): ManualGame {
            return ManualGame(
                id = GAME_IDS.getOrElse(index) { "?" },
                numbers = emptyList(),
                isAuto = isAuto
            )
        }

        /**
         * 게임 ID 목록
         */
        fun getGameIds(): List<String> = GAME_IDS
    }
}

/**
 * 수동 입력 화면 UI 상태
 */
data class ManualInputUiState(
    val currentRound: Int = 0,
    val games: List<ManualGame> = listOf(ManualGame.create(0)),
    val selectedGameIndex: Int = 0,
    val defaultIsAuto: Boolean = false,
    val isLoading: Boolean = false,
    val showCompleteBottomSheet: Boolean = false,
    val showRoundSelectionBottomSheet: Boolean = false,
    val showExitConfirmDialog: Boolean = false,
    val showDeleteGameDialog: Int? = null, // 삭제할 게임 인덱스
    val upcomingRound: Int = 0, // 미추첨 회차
    val availableRounds: List<Int> = emptyList() // 추첨된 회차 목록
) {
    /**
     * 현재 선택된 게임
     */
    val currentGame: ManualGame
        get() = games.getOrElse(selectedGameIndex) { ManualGame.create(0) }

    /**
     * 게임 추가 가능 여부 (최대 5개)
     */
    val canAddGame: Boolean
        get() = games.size < 5

    /**
     * 완료된 게임 수
     */
    val completedGamesCount: Int
        get() = games.count { it.isComplete }

    /**
     * 저장되지 않은 변경사항 존재 여부
     */
    val hasUnsavedChanges: Boolean
        get() = games.any { it.numbers.isNotEmpty() }

    /**
     * 저장 가능 여부 (현재 게임이 완료되어야 함)
     */
    val canSave: Boolean
        get() = currentGame.isComplete

    /**
     * 다른 게임 추가/이동 버튼 표시 여부
     * - 새 게임 추가 가능 (5개 미만)
     * - 또는 미완료 게임이 존재
     */
    val canAddOrMoveToNextGame: Boolean
        get() {
            if (canAddGame) return true
            // 현재 게임 이후에 미완료 게임이 있는지 확인
            return games.drop(selectedGameIndex + 1).any { !it.isComplete }
        }
}

/**
 * 수동 입력 화면 이벤트
 */
sealed class ManualInputEvent {
    /**
     * 화면 초기화 (진입 시)
     */
    data class Initialize(val round: Int) : ManualInputEvent()

    /**
     * 번호 선택
     */
    data class SelectNumber(val number: Int) : ManualInputEvent()

    /**
     * 번호 선택 해제
     */
    data class DeselectNumber(val number: Int) : ManualInputEvent()

    /**
     * 게임 탭 선택
     */
    data class SelectGame(val index: Int) : ManualInputEvent()

    /**
     * 새 게임 추가
     */
    data object AddGame : ManualInputEvent()

    /**
     * 게임 삭제
     */
    data class RemoveGame(val index: Int) : ManualInputEvent()

    /**
     * 자동/수동 모드 설정
     */
    data class SetAutoMode(val isAuto: Boolean) : ManualInputEvent()

    /**
     * 현재 게임 초기화
     */
    data object ResetCurrentGame : ManualInputEvent()

    /**
     * 저장 확인 (GameCompleteBottomSheet 표시)
     */
    data object ShowSaveConfirm : ManualInputEvent()

    /**
     * 저장 (실제 저장 실행)
     */
    data object Save : ManualInputEvent()

    /**
     * 완료 바텀시트 닫기
     */
    data object DismissBottomSheet : ManualInputEvent()

    /**
     * 바텀시트에서 게임 추가 확인
     */
    data object ConfirmAddGameFromBottomSheet : ManualInputEvent()

    /**
     * 회차 변경
     */
    data class ChangeRound(val round: Int) : ManualInputEvent()

    /**
     * 회차 선택 바텀시트 표시
     */
    data object ShowRoundSelection : ManualInputEvent()

    /**
     * 회차 선택 바텀시트 닫기
     */
    data object DismissRoundSelection : ManualInputEvent()

    /**
     * 뒤로가기 처리
     */
    data object OnBackPressed : ManualInputEvent()

    /**
     * 나가기 확인 다이얼로그 닫기
     */
    data object DismissExitConfirmDialog : ManualInputEvent()

    /**
     * 나가기 확인
     */
    data object ConfirmExit : ManualInputEvent()

    /**
     * 삭제 확인 다이얼로그 표시
     */
    data class ShowDeleteGameDialog(val index: Int) : ManualInputEvent()

    /**
     * 삭제 확인 다이얼로그 닫기
     */
    data object DismissDeleteGameDialog : ManualInputEvent()

    /**
     * 삭제 확인
     */
    data object ConfirmDeleteGame : ManualInputEvent()
}

/**
 * 수동 입력 화면 사이드 이펙트
 */
sealed class ManualInputEffect {
    /**
     * 스낵바 표시
     */
    data class ShowSnackbar(val message: String) : ManualInputEffect()

    /**
     * 뒤로 네비게이션
     */
    data object NavigateBack : ManualInputEffect()

    /**
     * 저장 성공
     */
    data class SaveSuccess(val count: Int) : ManualInputEffect()

    /**
     * 햅틱 피드백
     */
    data object Haptic : ManualInputEffect()

    /**
     * 성공 햅틱 (6개 완료 시)
     */
    data object HapticSuccess : ManualInputEffect()
}
