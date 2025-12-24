package com.mnn.cameranote.navigation

object Destinations {
    const val CAMERA_ROUTE = "camera"
    const val GALLERY_ROUTE = "gallery"
    const val DETAIL_ROUTE = "detail/{id}"
    fun createDetailRoute(id: Long) = "detail/$id"
}