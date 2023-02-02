package com.wutsi.application.feed.model

data class ProductModel(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val availability: String = "",
    val condition: String = "",
    val price: String = "",
    val salePrice: String? = null,
    val link: String = "",
    val imageLink: String? = null,
    val brand: String? = null,
    val additionalImageLink: List<String> = emptyList(),
    val googleProductCategory: Long? = null,
)
