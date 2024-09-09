package com.example.habitchain.data.local

import androidx.room.*
import com.example.habitchain.data.model.Habit
import com.example.habitchain.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): Habit?

    @Update
    suspend fun updateHabit(habit: Habit): Int

    @Delete
    suspend fun deleteHabit(habit: Habit): Int

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitByIdFlow(habitId: Int): Flow<Habit>

    @Query("SELECT * FROM habits WHERE category = :category")
    suspend fun getHabitsByCategory(category: String): List<Habit>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getTotalHabitCount(): Int

    @Query("SELECT COUNT(*) FROM habit_completions WHERE date(completion_date / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')")
    suspend fun getCompletedHabitsCount(): Int

    @Query("""
        SELECT 
            strftime('%w', date(completion_date / 1000, 'unixepoch', 'localtime')) AS day_of_week,
            COUNT(*) AS completion_count
        FROM habit_completions
        WHERE completion_date >= :startDate
        GROUP BY day_of_week
        ORDER BY day_of_week
    """
    )
    suspend fun getHabitCompletionsForLastWeek(startDate: Long): List<DayCompletion>

    @Query("SELECT * FROM habit_completions WHERE completion_date BETWEEN :startDate AND :endDate")
    suspend fun getHabitCompletionsForDateRange(startDate: Long, endDate: Long): List<HabitCompletion>

    @Query("DELETE FROM habit_completions WHERE completion_date BETWEEN :startDate AND :endDate")
    suspend fun removeHabitCompletionsForDateRange(startDate: Long, endDate: Long)

    @Query("DELETE FROM habit_completions WHERE habit_id = :habitId AND completion_date >= :todayStart")
    suspend fun removeHabitCompletionForToday(habitId: Int, todayStart: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletion)
}

data class DayCompletion(
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "completion_count") val completionCount: Int
)