package com.mnn.cameranote.navigation

object Destinations {
    const val CAMERA_ROUTE = "camera"
    const val GALLERY_ROUTE = "gallery"
    const val DETAIL_ROUTE = "detail/{id}"
    const val MESSAGE_INFO_ROUTE = "info/{id}"
    const val EDIT_TITLE_ROUTE = "edit/title/{id}"
    const val EDIT_DETAIL_ROUTE = "edit/detail/{id}"
    fun createDetailRoute(id: Long) = "detail/$id"
    fun createInfoRoute(id: Long) = "info/$id"
    fun createEditTitleRoute(id: Long) = "edit/title/$id"
    fun createEditDetailRoute(id: Long) = "edit/detail/$id"
}