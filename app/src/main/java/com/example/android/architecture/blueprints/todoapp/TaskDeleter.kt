package com.example.android.architecture.blueprints.todoapp

import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.*

/**
 * Data structure to keep track of how much time is left before an item gets permanently deleted
 * @author August Gruneisen
 */
class TaskDeleter {

    private val secondsToUndoDeletion: Int = 3

    private val timeRemainingToUndo: MutableMap<String, Int> = mutableMapOf()

    private val scheduledDeletions: MutableMap<String, Job> = mutableMapOf()

    /**
     * Used by the view layer to display the delete button text
     */
    fun getDeletionStatus(id: String): String {
        return timeRemainingToUndo[id]?.let {
            "$it - Undo"
        } ?: "Delete"
    }

    /**
     * Launches a cancellable job to track deletion of a task after [secondsToUndoDeletion] seconds
     *
     * @param task the task to be deleted
     * @param scope the scope to launch this job in
     * @param progressUpdate called every second until the item is ready to be deleted. Also called if the deletion is cancelled
     * @param onTimesUp TIMES UP! Item is ready to be deleted
     */
    fun scheduleTaskForDeletion(task: Task, scope: CoroutineScope, progressUpdate: () -> Unit, onTimesUp: () -> Unit) {
        scheduledDeletions[task.id] = scope.launch {
            try {
                timeRemainingToUndo[task.id] = secondsToUndoDeletion
                progressUpdate()

                repeat(secondsToUndoDeletion) {
                    delay(1000)
                    updateStatusAfterOneSecond(task.id)
                    progressUpdate()
                }

                onTimesUp()
            } catch (e: CancellationException) {
                timeRemainingToUndo.remove(task.id)
                progressUpdate()
            }
        }
    }

    /**
     * Cancels a task previously scheduled for deletion
     */
    fun undoTaskDeletion(task: Task) {
        val job = scheduledDeletions[task.id]
        job?.cancel()
        scheduledDeletions.remove(task.id)
    }

    private fun updateStatusAfterOneSecond(id: String) {
        timeRemainingToUndo[id]?.let {
            if (it - 1 == 0) timeRemainingToUndo.remove(id) // times up
            else timeRemainingToUndo[id] = it - 1
        }
    }
}