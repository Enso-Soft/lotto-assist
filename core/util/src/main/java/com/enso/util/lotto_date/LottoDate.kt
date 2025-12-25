package com.enso.util.lotto_date

import java.util.Calendar
import java.util.Calendar.DATE
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.DECEMBER
import java.util.Calendar.FRIDAY
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONDAY
import java.util.Calendar.MONTH
import java.util.Calendar.SATURDAY
import java.util.Calendar.SECOND
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY
import java.util.Calendar.WEDNESDAY
import java.util.Calendar.YEAR
import java.util.concurrent.TimeUnit

object LottoDate {
    // 첫 로또 추첨일을 Calendar 객체로 초기화 (2002년 12월 7일 00:00:00)
    // 모든 회차 계산의 기준점이 됨
    private val FIRST_DRAW_CALENDAR: Calendar = Calendar.getInstance().apply {
        set(2002, DECEMBER, 7, 0, 0, 0)
        set(MILLISECOND, 0)
    }

    // 로또 추첨 시각 상수 (오후 8시 45분)
    private val DRAW_HOUR = 20
    private val DRAW_MINUTE = 45

    /**
     * 현재 날짜와 시간을 기준으로 로또 회차를 계산
     * 토요일인 경우 추첨 시간(20:45) 이전이면 이전 주 회차를 반환
     * @return Int 현재 로또 회차
     */
    fun getCurrentDrawNumber(): Int {
        val now = Calendar.getInstance()

        // 오늘이 토요일이고 아직 추첨 시간 전이면 이전 주 번호를 반환
        if (now.get(DAY_OF_WEEK) == SATURDAY) {
            val drawTime = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                set(HOUR_OF_DAY, DRAW_HOUR)
                set(MINUTE, DRAW_MINUTE)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }

            if (now.before(drawTime)) {
                now.add(DATE, -7)  // 일주일 전으로 설정
            }
        }

        return getDrawNumberByDate(now)
    }

    /**
     * 주어진 날짜의 로또 회차를 계산
     * 첫 추첨일로부터 몇 주가 지났는지 계산하여 회차 결정
     * @param date 계산할 날짜의 Calendar 객체
     * @return Int 해당 날짜의 로또 회차
     */
    private fun getDrawNumberByDate(date: Calendar): Int {
        val diffInMillis = date.timeInMillis - FIRST_DRAW_CALENDAR.timeInMillis
        val diffInWeeks = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 7
        return diffInWeeks.toInt() + 1
    }

    /**
     * 특정 회차의 추첨일을 계산
     * 첫 추첨일로부터 (회차-1)주 만큼의 날짜를 더함
     * @param drawNumber 조회할 로또 회차
     * @return Calendar 해당 회차의 추첨일 Calendar 객체
     */
    fun getDrawDateByNumber(drawNumber: Int): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = FIRST_DRAW_CALENDAR.timeInMillis
            add(DATE, (drawNumber - 1) * 7)
        }
    }

    /**
     * 특정 회차의 추첨일시를 계산 (20:45 포함)
     * @param drawNumber 조회할 로또 회차
     * @return Calendar 해당 회차의 추첨일시 Calendar 객체
     */
    fun getDrawDateTimeByNumber(drawNumber: Int): Calendar {
        return getDrawDateByNumber(drawNumber).apply {
            set(HOUR_OF_DAY, DRAW_HOUR)
            set(MINUTE, DRAW_MINUTE)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }
    }

    /**
     * 주어진 날짜가 추첨일(토요일)인지 확인
     * @param date 확인할 날짜의 Calendar 객체 (기본값: 현재 날짜)
     * @return Boolean 토요일이면 true, 아니면 false
     */
    fun isDrawDay(date: Calendar = Calendar.getInstance()): Boolean {
        return date.get(DAY_OF_WEEK) == SATURDAY
    }

    /**
     * 다음 추첨 회차를 반환 (미추첨 회차)
     * 티켓 등록 시 사용할 회차
     * @return Int 다음 추첨 회차
     */
    fun getUpcomingDrawRound(): Int = getCurrentDrawNumber() + 1

    /**
     * 다음 추첨일시를 계산
     * 현재 시점 이후의 가장 가까운 토요일 20:45를 계산
     * 현재가 토요일 20:45 이후라면 다음 주 토요일 반환
     * @return Calendar 다음 추첨 일시의 Calendar 객체
     */
    fun getNextDrawDateTime(): Calendar {
        val now = Calendar.getInstance()

        // 다음 토요일 찾기
        val nextDraw = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis

            // 현재 요일부터 다음 토요일까지 날짜 더하기
            val daysUntilSaturday = when (get(DAY_OF_WEEK)) {
                SUNDAY -> 6
                MONDAY -> 5
                TUESDAY -> 4
                WEDNESDAY -> 3
                THURSDAY -> 2
                FRIDAY -> 1
                SATURDAY -> 0
                else -> 0
            }

            add(DATE, daysUntilSaturday)
            set(HOUR_OF_DAY, DRAW_HOUR)
            set(MINUTE, DRAW_MINUTE)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }

        // 현재 시간이 이번주 추첨 시간 이후라면 다음 주로 설정
        if (now.after(nextDraw)) {
            nextDraw.add(DATE, 7)
        }

        return nextDraw
    }

    /**
     * 다음 추첨까지 남은 시간을 밀리초 단위로 계산
     * @return Long 다음 추첨까지 남은 밀리초
     */
    fun getTimeUntilNextDraw(): Long {
        val now = Calendar.getInstance()
        val nextDraw = getNextDrawDateTime()
        return nextDraw.timeInMillis - now.timeInMillis
    }

    /**
     * Calendar 객체의 날짜를 "YYYY-MM-DD" 형식의 문자열로 변환
     * @return String 포맷팅된 날짜 문자열
     */
    fun Calendar.formatDate(): String {
        return "${get(YEAR)}-${get(MONTH) + 1}-${get(DATE)}"
    }
}
