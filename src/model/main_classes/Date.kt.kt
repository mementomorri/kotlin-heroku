package me.mementomorri.model.main_classes

import kotlinx.serialization.Serializable
import org.h2.store.Data
import org.jetbrains.exposed.sql.`java-time`.Date

@Serializable
data class AppDate(
    val year: Int,
    val month: Int,
    val dayOfMonth:Int,
){
    fun plusDays(days:Int):AppDate{
        if (month==12 && dayOfMonth+days>31) return AppDate(year+1,1,dayOfMonth+days-31)
        if (month==2 && dayOfMonth+days >28) return AppDate(this.year,this.month+1, this.dayOfMonth+days-28)
        else AppDate(this.year,this.month, this.dayOfMonth+days)

        if ((this.dayOfMonth+days)<= 30) return AppDate(this.year,this.month, this.dayOfMonth+days)
        else{
            if (month%2==0 &&  month!=2){
                return AppDate(this.year,this.month+days, this.dayOfMonth+days-30)
            } else{
                if (month==2 &&(month+days > 31)) {
                    return AppDate(this.year,this.month+days, this.dayOfMonth+days-31)
                }
            }
        }
        return AppDate(this.year,this.month, this.dayOfMonth+days)
    }

    override fun equals(other: Any?): Boolean {
        other as AppDate
        return (this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth)
    }

    fun nextWeek():AppDate{
        if (month==2 && dayOfMonth+7 >28) return AppDate(this.year,this.month+1, this.dayOfMonth+7-28)
        else AppDate(this.year,this.month, this.dayOfMonth+7)
        if ((this.dayOfMonth+7)<= 30) return AppDate(this.year,this.month, this.dayOfMonth+7)
        else{
            if (month%2==0 &&  month!=2){
                return AppDate(this.year,this.month+1, this.dayOfMonth+7-30)
            } else{
                if (month==2 &&(month+7 > 31)) {
                    return AppDate(this.year,this.month+1, this.dayOfMonth+7-31)
                }
            }
        }
        return AppDate(this.year,this.month, this.dayOfMonth+7)
    }

    override fun toString(): String {
        return "$year-$month-$dayOfMonth"
    }
}