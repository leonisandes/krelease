package io.github.leonisandes.krelease.utils

fun readJsonResource(fileName: String) = ClassLoader.getSystemResource("json/$fileName.json").readText()
