package com.d2d.challenge.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d2d.challenge.data.entity.Payload
import com.d2d.challenge.data.repository.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel class is responsible for collecting data from service layer and provide it to view layer. It also
 * works as bridge between view and data.
 * HiltViewModel will be consumed by activity or fragment which is created by viewmodel factory
 * View model is created by constructor injection which inject dependencies, generally repository
 *
 * @param mapsRepository passed as a primary constructor used for consuming data from service and posting it to
 * observable.
 */

@HiltViewModel
class MapsViewModel @Inject constructor(private val mapsRepository: MapsRepository) : ViewModel() {

    private val _payload = MutableLiveData<Payload>()

    val payload: LiveData<Payload>
        get() = _payload

    init {
        subscribeToSocketEvents()
    }


    /**
     * Used for launch coroutine and posting live data within the viewModelScope. Also handle success and error
     * event according to server request.
     */
    private fun subscribeToSocketEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mapsRepository.getSocket().consumeEach {
                    if (it.exception == null) {
                        _payload.postValue(it.payload)
                    } else {
                        onSocketError(it.exception)
                    }
                }
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
            }
        }
    }


    /**
     * Receive error event occurred as a result of service request.
     * @param ex throwable exception object to process actual error type
     */
    private fun onSocketError(ex: Throwable) {
        Log.i(MapsViewModel::class.java.simpleName, "_log onSocketError :  ${ex.message}")
    }

    /**
     * Called when view model scope is cleared from activity. Also responsible for disconnect socket from client end.
     */
    override fun onCleared() {
        super.onCleared()
        mapsRepository.disconnectSocket()
    }


}