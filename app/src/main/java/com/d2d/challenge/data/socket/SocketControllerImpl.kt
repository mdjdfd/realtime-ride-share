package com.d2d.challenge.data.socket

import android.util.Log
import com.d2d.challenge.common.Constant.NORMAL_CLOSURE_STATUS
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import javax.inject.Inject

class SocketControllerImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val request: Request,
    private val socketCallback: SocketCallback,
) : ISocketController {

    private var webSocket: WebSocket? = null


    override fun startSocket(): Channel<SocketUpdate> =
        with(socketCallback) {
            webSocket = okHttpClient.newWebSocket(request, socketCallback)
            okHttpClient.dispatcher.executorService.shutdown()
            this@with._socketChannel
        }

    override fun stopSocket() {
        webSocket?.close(NORMAL_CLOSURE_STATUS, null)
        webSocket = null
        socketCallback._socketChannel?.close()
    }
}