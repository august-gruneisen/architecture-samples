# "Inline delete" feature

1. Clicking "delete" will show a countdown timer and an undo button
2. Clicking "undo" will cancel/hide the timer and button
3. Upon timing out, the item will be deleted

[Download the APK](https://github.com/august-gruneisen/architecture-samples/raw/delete-task-feature/.github/support-files/app-prod-debug.apk) to test.

<p align="center">
  <img src=".github/support-files/deleting-items.gif" width="200" alt="Deleting items"/>
	<img src=".github/support-files/undo-deleting-items.gif" width="200" alt="Undo deleting items"/>
</p>

Main challenge:
- Allowing multiple undo timers counting down in parallel

Quick fix can be seen with commit [2ea3c59](https://github.com/august-gruneisen/architecture-samples/commit/2ea3c591273cf4a22fe43e8b32356bbfddc0ac52), however holding a reference to the view in the view model is a design smell. This implementation does not hold for configuration changes, as the coroutine will continue to update the text of an obsolete view (since a new view has been created). The user loses the ability to undo, and the item is still deleted after 3 seconds.

More robust solution is shown with commit [9ebb616](https://github.com/august-gruneisen/architecture-samples/commit/9ebb616fb2181647333607fcb0d219337eee154c#diff-849fad0edf950a6d36f71ceb065d260c6c0069923b6c714e1d896591818a3d3e).

__Noteworthy:__ The [TaskDeleter](https://github.com/august-gruneisen/architecture-samples/blob/delete-task-feature/app/src/main/java/com/example/android/architecture/blueprints/todoapp/util/TaskDeleter.kt) class depends only on Kotlin Coroutines and the `Task` type, allowing platform-specific functionality (i.e. deleting tasks from local storage and updating the view layer) to be specified by the caller. Because of this, it would scale well in a multiplatform project using a shared codebase (KMM).

__Shortcut:__ Task says "delete" again right before it actually gets deleted due to [TaskDeleter::24](https://github.com/august-gruneisen/architecture-samples/blob/delete-task-feature/app/src/main/java/com/example/android/architecture/blueprints/todoapp/util/TaskDeleter.kt#L24)

__Optimization:__ Use adapter position to determine which list item to update [here](https://github.com/august-gruneisen/architecture-samples/blob/delete-task-feature/app/src/main/java/com/example/android/architecture/blueprints/todoapp/tasks/TasksFragment.kt#L167)
