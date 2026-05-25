package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import com.tkachukmo.bandresearchapp.core.player.AudioController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    val audioController: AudioController
) : ViewModel()