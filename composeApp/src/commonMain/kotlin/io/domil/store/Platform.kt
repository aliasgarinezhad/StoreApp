package io.domil.store

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform