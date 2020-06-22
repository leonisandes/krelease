package io.github.leonisandes.krelease.extensions

import com.github.kittinunf.fuel.core.Request

fun Request.responseJson(): Pair<Int, String> = with(this.responseString()) {
    this.second.statusCode to String(this.second.data, Charsets.UTF_8)
}
