package com.pinkcloud.memento.edit

import dagger.assisted.AssistedFactory

@AssistedFactory
interface EditViewModelFactory {
    fun create(memoId: Long): EditViewModel
}