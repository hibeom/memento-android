package com.pinkcloud.memento.ui.edit

import dagger.assisted.AssistedFactory

@AssistedFactory
interface EditViewModelFactory {
    fun create(memoId: Long): EditViewModel
}