package com.example.android.architecture.blueprints.todoapp.util

import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TaskDeleter]
 */
@ExperimentalCoroutinesApi
class TaskDeleterTest {

    // Subject under test
    private lateinit var taskDeleter: TaskDeleter

    // Task to be deleted
    private lateinit var task: Task

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupTaskDeleter() {
        taskDeleter = TaskDeleter()

        // Create a new task
        task = Task("Title1", "Description1")
    }

    @Test
    fun beforeDeletion_getCorrectTextToDisplay() {
        // Verify the text to display before scheduling the task for deletion
        val deletionStatusBefore = taskDeleter.getDeletionStatus(task.id)
        assertEquals(deletionStatusBefore, "Delete")
    }

    @Test
    fun duringDeletion_getCorrectTextToDisplay() {
        // Schedule the deletion and immediately pause scope execution
        taskDeleter.scheduleTaskForDeletion(task, mainCoroutineRule, {}, {})
        mainCoroutineRule.pauseDispatcher()

        // Verify the text to display
        val textToDisplayImmediatelyAfter = taskDeleter.getDeletionStatus(task.id)
        assertEquals(textToDisplayImmediatelyAfter, "3 - Undo")

        // Let 1 second pass
        mainCoroutineRule.advanceTimeBy(1000)
        // Verify the text to display after 1 second
        val textToDisplayAfterOneSecond = taskDeleter.getDeletionStatus(task.id)
        assertEquals(textToDisplayAfterOneSecond, "2 - Undo")

        // Let 2 seconds pass
        mainCoroutineRule.advanceTimeBy(2000)
        // Verify the text to display after 3 seconds
        val textToDisplayAfterThreeSeconds = taskDeleter.getDeletionStatus(task.id)
        assertEquals(textToDisplayAfterThreeSeconds, "Delete") // task has been deleted
    }

    @Test
    fun undoDeletion_getCorrectTextToDisplay() {
        // Schedule the deletion and immediately pause scope execution
        taskDeleter.scheduleTaskForDeletion(task, mainCoroutineRule, {}, {})
        mainCoroutineRule.pauseDispatcher()

        // Verify the text to display
        val textToDisplayImmediatelyAfter = taskDeleter.getDeletionStatus(task.id)
        assertEquals(textToDisplayImmediatelyAfter, "3 - Undo")

        // Undo deletion and allow the scope to execute
        taskDeleter.undoTaskDeletion(task)
        mainCoroutineRule.runCurrent()

        // Verify the text to display after canceling the deletion
        val textToDisplayAfterCanceling = taskDeleter.getDeletionStatus(task.id)
        assertEquals(textToDisplayAfterCanceling, "Delete") // no longer scheduled
    }
}