package com.lianpo.clock.ui.privatetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianpo.clock.data.database.dao.MemoDao
import com.lianpo.clock.data.database.dao.PrivateRecordDao
import com.lianpo.clock.data.database.entity.Memo
import com.lianpo.clock.data.database.entity.PrivateRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DailyCount(
    val date: Long,
    val count: Int
)

@HiltViewModel
class PrivateViewModel @Inject constructor(
    private val privateRecordDao: PrivateRecordDao,
    private val memoDao: MemoDao
) : ViewModel() {

    private val _todayCount = MutableStateFlow(0)
    val todayCount: StateFlow<Int> = _todayCount.asStateFlow()

    private val _weekCount = MutableStateFlow(0)
    val weekCount: StateFlow<Int> = _weekCount.asStateFlow()

    private val _monthCount = MutableStateFlow(0)
    val monthCount: StateFlow<Int> = _monthCount.asStateFlow()

    private val _yearCount = MutableStateFlow(0)
    val yearCount: StateFlow<Int> = _yearCount.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _selectedMonthRecords = MutableStateFlow<List<PrivateRecord>>(emptyList())
    val selectedMonthRecords: StateFlow<List<PrivateRecord>> = _selectedMonthRecords.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedMonthCount = MutableStateFlow(0)
    val selectedMonthCount: StateFlow<Int> = _selectedMonthCount.asStateFlow()

    private val _weeklyData = MutableStateFlow<List<DailyCount>>(emptyList())
    val weeklyData: StateFlow<List<DailyCount>> = _weeklyData.asStateFlow()

    private val _monthlyData = MutableStateFlow<List<DailyCount>>(emptyList())
    val monthlyData: StateFlow<List<DailyCount>> = _monthlyData.asStateFlow()

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _showAddMemo = MutableStateFlow(false)
    val showAddMemo: StateFlow<Boolean> = _showAddMemo.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    private val _selectedDayRecords = MutableStateFlow<List<PrivateRecord>>(emptyList())
    val selectedDayRecords: StateFlow<List<PrivateRecord>> = _selectedDayRecords.asStateFlow()

    init {
        loadStats()
        loadMemos()
        loadSelectedMonthRecords()
    }

    private fun loadMemos() {
        viewModelScope.launch {
            memoDao.getAllMemos().collect { list ->
                _memos.value = list
            }
        }
    }

    fun addMemo(content: String) {
        viewModelScope.launch {
            memoDao.insert(Memo(content = content))
            _showAddMemo.value = false
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            memoDao.delete(memo)
        }
    }

    fun togglePinMemo(memo: Memo) {
        viewModelScope.launch {
            memoDao.update(memo.copy(isPinned = !memo.isPinned))
        }
    }

    fun showAddMemoDialog() {
        _showAddMemo.value = true
    }

    fun hideAddMemoDialog() {
        _showAddMemo.value = false
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
        loadSelectedDayRecords()
    }

    private fun loadSelectedDayRecords() {
        viewModelScope.launch {
            val day = _selectedDay.value ?: return@launch
            val cal = Calendar.getInstance()
            cal.set(_selectedYear.value, _selectedMonth.value, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis

            cal.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = cal.timeInMillis

            _selectedDayRecords.value = _selectedMonthRecords.value.filter { record ->
                record.timestamp >= dayStart && record.timestamp < dayEnd
            }.sortedBy { it.timestamp }
        }
    }

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadSelectedMonthRecords()
    }

    fun previousMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, -1)
        _selectedYear.value = cal.get(Calendar.YEAR)
        _selectedMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value = null
        _selectedDayRecords.value = emptyList()
        loadSelectedMonthRecords()
    }

    fun nextMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, 1)
        _selectedYear.value = cal.get(Calendar.YEAR)
        _selectedMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value = null
        _selectedDayRecords.value = emptyList()
        loadSelectedMonthRecords()
    }

    private fun loadSelectedMonthRecords() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(_selectedYear.value, _selectedMonth.value, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val monthEnd = cal.timeInMillis

            privateRecordDao.getRecordsBetween(monthStart, monthEnd).collect { records ->
                _selectedMonthRecords.value = records
                _selectedMonthCount.value = records.size
            }
        }
    }

    fun getMonthName(): String {
        val months = listOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")
        return "${_selectedYear.value}年 ${months[_selectedMonth.value]}"
    }

    fun loadStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val now = System.currentTimeMillis()

            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val todayEnd = cal.timeInMillis

            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val weekStart = cal.timeInMillis

            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val yearStart = cal.timeInMillis

            _todayCount.value = privateRecordDao.getCountBetween(todayStart, todayEnd)
            _weekCount.value = privateRecordDao.getCountBetween(weekStart, now)
            _monthCount.value = privateRecordDao.getCountBetween(monthStart, now)
            _yearCount.value = privateRecordDao.getCountBetween(yearStart, now)

            privateRecordDao.getAllRecords().collect { records ->
                _totalCount.value = records.size
            }
        }

        viewModelScope.launch {
            val weekly = mutableListOf<DailyCount>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            for (i in 0 until 7) {
                val dayStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 1)
                val dayEnd = cal.timeInMillis
                val count = privateRecordDao.getCountBetween(dayStart, dayEnd)
                weekly.add(DailyCount(dayStart, count))
            }
            _weeklyData.value = weekly
        }

        viewModelScope.launch {
            val monthly = mutableListOf<DailyCount>()
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (i in 0 until daysInMonth) {
                val dayStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 1)
                val dayEnd = cal.timeInMillis
                val count = privateRecordDao.getCountBetween(dayStart, dayEnd)
                monthly.add(DailyCount(dayStart, count))
            }
            _monthlyData.value = monthly
        }
    }

    fun addRecord() {
        viewModelScope.launch {
            privateRecordDao.insert(PrivateRecord())
            loadStats()
            loadSelectedMonthRecords()
        }
    }

    fun deleteRecord(record: PrivateRecord) {
        viewModelScope.launch {
            privateRecordDao.delete(record)
            loadStats()
            loadSelectedMonthRecords()
        }
    }

    fun updateRecordMood(record: PrivateRecord, mood: String) {
        viewModelScope.launch {
            privateRecordDao.update(record.copy(mood = mood))
            loadStats()
            loadSelectedMonthRecords()
        }
    }

    fun updateRecordMemo(record: PrivateRecord, memo: String) {
        viewModelScope.launch {
            privateRecordDao.update(record.copy(memo = memo))
            loadStats()
            loadSelectedMonthRecords()
        }
    }

    fun getFrequency(): String {
        val yearCount = _yearCount.value
        if (yearCount == 0) return "暂无数据"
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val yearStart = cal.timeInMillis
        val daysPassed = ((now - yearStart) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
        val avgPerDay = yearCount.toFloat() / daysPassed
        val avgPerWeek = yearCount.toFloat() / (daysPassed / 7f).coerceAtLeast(1f)
        val avgPerMonth = yearCount.toFloat() / 12f
        return String.format("日均 %.1f | 周均 %.1f | 月均 %.1f", avgPerDay, avgPerWeek, avgPerMonth)
    }
}