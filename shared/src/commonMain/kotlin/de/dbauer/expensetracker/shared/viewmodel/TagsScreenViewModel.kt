package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.Tag
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TagsScreenViewModel(
    private val expenseRepository: IExpenseRepository,
) : ViewModel() {
    private var _tags = mutableStateListOf<Tag>()
    val tags: List<Tag>
        get() = _tags.sortedBy { it.title }
    var showAddOrEditTagDialog by mutableStateOf(false)
        private set
    private var tagToEdit by mutableStateOf<Tag?>(null)
    val isNewTag: Boolean
        get() = tagToEdit == null
    private var pendingTagDeletion: Pair<Tag, Job>? = null

    var tagTitle by mutableStateOf("")
        private set
    var tagTitleError by mutableStateOf(false)
        private set
    var tagColor by mutableLongStateOf(0L)
        private set
    var tagColorError by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            expenseRepository.allTags.collect {
                _tags.clear()
                _tags.addAll(it.filter { tag -> tag.id != pendingTagDeletion?.first?.id })
            }
        }
    }

    fun onAddNewTag() {
        showAddOrEditTagDialog = true
    }

    fun onDismissAddNewTagDialog() {
        showAddOrEditTagDialog = false
        tagTitle = ""
        tagTitleError = false
        tagColor = 0L
        tagColorError = false
        tagToEdit = null
    }

    fun onTagTitleChange(title: String) {
        tagTitle = title
        tagTitleError = false
    }

    fun onTagColorChange(color: Long) {
        tagColor = color
        tagColorError = false
    }

    fun onConfirmAddNewTag() {
        tagTitleError = tagTitle.isBlank()
        tagColorError = tagColor == 0L
        if (!tagTitleError && !tagColorError) {
            viewModelScope.launch {
                if (isNewTag) {
                    val newTag = Tag(tagTitle, tagColor)
                    expenseRepository.insert(newTag)
                } else {
                    tagToEdit?.copy(title = tagTitle, color = tagColor)?.let { tag ->
                        expenseRepository.update(tag)
                    }
                }
            }
            onDismissAddNewTagDialog()
        }
    }

    fun onEditTag(tag: Tag) {
        tagTitle = tag.title
        tagColor = tag.color
        tagToEdit = tag
        showAddOrEditTagDialog = true
    }

    fun onDeleteTag(tag: Tag) {
        finalizePendingDeletion()
        _tags.remove(tag)
        val job =
            viewModelScope.launch {
                delay(8000)
                expenseRepository.delete(tag)
            }
        pendingTagDeletion = tag to job
    }

    fun onUndoDeleteTag(tag: Tag) {
        pendingTagDeletion?.let { (_, job) ->
            job.cancel()
            pendingTagDeletion = null
        }
        _tags.add(tag)
    }

    override fun onCleared() {
        super.onCleared()
        finalizePendingDeletion()
    }

    private fun finalizePendingDeletion() {
        pendingTagDeletion?.let { (tag, job) ->
            job.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                expenseRepository.delete(tag)
            }
            pendingTagDeletion = null
        }
    }
}
