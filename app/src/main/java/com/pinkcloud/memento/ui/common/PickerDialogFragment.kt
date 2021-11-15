package com.pinkcloud.memento.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.pinkcloud.memento.R
import com.pinkcloud.memento.databinding.LayoutDialogPickerBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class PickerDialogFragment(private val listener: PickDatetimeListener) : DialogFragment() {

    interface PickDatetimeListener {
        fun onPickDatetime(millis: Long)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.TransparentDialog)
            val inflater = requireActivity().layoutInflater

            val timeMillis = System.currentTimeMillis()
            val instant = Instant.ofEpochMilli(timeMillis)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

            val year = date.year
            val month = date.monthValue
            val day = date.dayOfMonth
            var hour = String.format("%02d", date.hour % 12).toInt()
            if (hour == 0) hour = 12
            val minute = String.format("%02d", date.minute).toInt()
            val ampmVal = date.hour / 12

            val binding = LayoutDialogPickerBinding.inflate(inflater, null, false)

            val firstDate = LocalDate.of(year, month, day)
            val dateList = getDateList(firstDate)
            binding.pickerDate.apply {
                wrapSelectorWheel = false
                minValue = 0
                maxValue = dateList.size - 1
                value = 0
                displayedValues = dateList
            }
            val ampmList = arrayOf(getString(R.string.AM), getString(R.string.PM))
            binding.pickerAmpm.apply {
                minValue = 0
                maxValue = ampmList.size - 1
                value = ampmVal
                displayedValues = ampmList
            }
            binding.pickerHour.apply {
                minValue = 1
                maxValue = 12
                value = hour
                setOnValueChangedListener { _, oldVal, newVal ->
                    if (oldVal == 12 && newVal == 11 || oldVal == 11 && newVal == 12) {
                        if (binding.pickerAmpm.value == 0) {
                            changeValueByOne(binding.pickerAmpm, true)
                        } else {
                            changeValueByOne(binding.pickerAmpm, false)
                        }
                    }
                }
            }
            binding.pickerMinute.apply {
                minValue = 0
                maxValue = 59
                value = minute
            }

            binding.buttonOk.setOnClickListener {
                val newDate = firstDate.plusDays(binding.pickerDate.value.toLong())
                var hour = binding.pickerHour.value % 12
                hour = if (binding.pickerAmpm.value == 0) hour else hour + 12
                val pickedDate = LocalDateTime.of(
                    newDate.year,
                    newDate.monthValue,
                    newDate.dayOfMonth,
                    hour,
                    binding.pickerMinute.value
                )
                listener.onPickDatetime(
                    pickedDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                dismiss()
            }
            binding.buttonCancel.setOnClickListener {
                dismiss()
            }
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun changeValueByOne(higherPicker: NumberPicker, increment: Boolean) {
        val method: Method
        try {
            // refelction call for
            // higherPicker.changeValueByOne(true);
            method = higherPicker.javaClass.getDeclaredMethod(
                "changeValueByOne",
                Boolean::class.javaPrimitiveType
            )
            method.setAccessible(true)
            method.invoke(higherPicker, increment)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    private fun getDateList(firstDate: LocalDate): Array<String> {
        val dateList = mutableListOf<String>()
        when (Locale.getDefault().language) {
            Locale.KOREA.language -> {
                for (i in 0..364) {
                    val newDate = firstDate.plusDays(i.toLong())
                    dateList.add(
                        newDate.format(
                            DateTimeFormatter.ofPattern("M월 d일 E")
                                .withLocale(Locale.forLanguageTag("ko"))
                        )
                    )
                }
            }
            else -> {
                for (i in 0..364) {
                    val newDate = firstDate.plusDays(i.toLong())
                    dateList.add(
                        "${newDate.dayOfWeek.toString().substring(0, 3)} ${
                            newDate.month.toString().substring(0, 3)
                        } ${newDate.dayOfMonth}"
                    )
                }
            }

        }
        return dateList.toTypedArray()
    }
}
