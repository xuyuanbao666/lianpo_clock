package com.lianpo.clock.ui.privatetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lianpo.clock.data.database.dao.PrivateRecordDao
import com.lianpo.clock.data.database.entity.PrivateRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PrivateViewModel @Inject constructor(
    private val privateRecordDao: PrivateRecordDao
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

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()

            // 今日
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val todayEnd = cal.timeInMillis

            // 本周
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val weekStart = cal.timeInMillis

            // 本月
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            // 本年
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val yearStart = cal.timeInMillis

            val now = System.currentTimeMillis()

            _todayCount.value = privateRecordDao.getCountBetween(todayStart, todayEnd)
            _weekCount.value = privateRecordDao.getCountBetween(weekStart, now)
            _monthCount.value = privateRecordDao.getCountBetween(monthStart, now)
            _yearCount.value = privateRecordDao.getCountBetween(yearStart, now)

            // 获取最近记录
            privateRecordDao.getAllRecords().collect { records ->
                _totalCount.value = records.size
                _recentRecords.value = records.take(20)
            }
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
        return String.format("日均 %.1f 次 | 周均 %.1f 次", avgPerDay, avgPerWeek)
    }
}