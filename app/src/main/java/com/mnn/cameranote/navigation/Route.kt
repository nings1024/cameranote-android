package com.mnn.cameranote.navigation

import kotlinx.serialization.Serializable

// 用密封类或简单的类来定义路由
sealed class Route {
    @Serializable object Camera : Route()
    @Serializable object Gallery : Route()

    @Serializable
    data class Detail(val id: Long) : Route()

    @Serializable
    data class MessageInfo(val id: Long) : Route()

    @Serializable
    data class EditTitle(val id: Long) : Route()

    @Serializable
    data class EditDetail(val id: Long) : Route()
}