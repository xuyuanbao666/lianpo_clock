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

    private val _recentRecords = MutableStateFlow<List<PrivateRecord>>(emptyList())
    val recentRecords: StateFlow<List<PrivateRecord>> = _recentRecords.asStateFlow()

    private val _weeklyData = MutableStateFlow<List<DailyCount>>(emptyList())
    val weeklyData: StateFlow<List<DailyCount>> = _weeklyData.asStateFlow()

    private val _monthlyData = MutableStateFlow<List<DailyCount>>(emptyList())
    val monthlyData: StateFlow<List<DailyCount>> = _monthlyData.asStateFlow()

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _showAddMemo = MutableStateFlow(false)
    val showAddMemo: StateFlow<Boolean> = _showAddMemo.asStateFlow()

    init {
        loadStats()
        loadMemos()
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
                _recentRecords.value = records.take(60)
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
        }
    }

    fun deleteRecord(record: PrivateRecord) {
        viewModelScope.launch {
            privateRecordDao.delete(record)
            loadStats()
        }
    }

    fun updateRecordMood(record: PrivateRecord, mood: String) {
        viewModelScope.launch {
            privateRecordDao.update(record.copy(mood = mood))
            loadStats()
        }
    }

    fun updateRecordMemo(record: PrivateRecord, memo: String) {
        viewModelScope.launch {
            privateRecordDao.update(record.copy(memo = memo))
            loadStats()
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