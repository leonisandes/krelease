package io.github.leonisandes.krelease.utils

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Method.POST
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.fuel.core.requests.DefaultRequest
import com.github.kittinunf.result.Result
import java.io.ByteArrayInputStream
import java.net.URL

fun request(method: Method, url: String, body: String = "", headers: Map<String, String> = emptyMap()): Request {
    val requestHeaders = Headers().apply { headers.forEach { set(it.key, it.value) } }
    val requestBody = DefaultBody.from({ ByteArrayInputStream(body.toByteArray(Charsets.UTF_8)) }, null)

    return DefaultRequest(method, URL(url), requestHeaders, _body = requestBody)
}

fun response(
    url: String,
    statusCode: Int,
    body: String = ""
): ResponseResultOf<String> {
    val request = DefaultRequest(POST, URL(url))
    val result = Result.success(body)
    val response = Response(URL(url), statusCode, body = DefaultBody.from({ body.byteInputStream() }, null))

    return ResponseResultOf(request, response, result)
}
