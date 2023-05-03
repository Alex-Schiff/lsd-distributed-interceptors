package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.Logger
import feign.Request
import feign.Response
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomUtils
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import java.io.IOException

internal class LsdFeignLoggerInterceptorShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val requestCaptor = mockk<RequestCaptor>()
    private val responseCaptor = mockk<ResponseCaptor>()
    private val request = mockk<Request>()
    private val response = mockk<Response>()
    private val underTest = LsdFeignLoggerInterceptor(requestCaptor, responseCaptor)
    private val level = Logger.Level.BASIC
    private val elapsedTime = RandomUtils.nextLong()

    @Test
    fun logsRequest() {
        underTest.logRequest("configKey", level, request)
        verify {  requestCaptor.captureRequestInteraction(request) }
    }

    @Test
    @Throws(IOException::class)
    fun logAndRebufferResponse() {
        every { responseCaptor.captureResponseInteraction(any(), eq<Long>(elapsedTime)) } returns
            easyRandom.nextObject(InterceptedInteraction::class.java).copy(body = null)

        underTest.logAndRebufferResponse("configKey", level, response, elapsedTime)

        verify { responseCaptor.captureResponseInteraction(response, elapsedTime) }
    }
}
