package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import org.springframework.messaging.Message

class MessagingHeaderRetriever(
    private val obfuscator: Obfuscator
) {
    fun retrieve(message: Message<*>): Map<String, Collection<String>> =
        obfuscator.obfuscate(message.headers.entries.associate {
            it.key to (it.value?.let { _ -> listOf(it.value.toString()) } ?: emptyList())
        })
}