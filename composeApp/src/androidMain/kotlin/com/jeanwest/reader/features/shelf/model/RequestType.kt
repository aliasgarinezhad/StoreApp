package com.jeanwest.reader.features.shelf.model

enum class RequestType {
    Product,
    Carton;

    override fun toString(): String {
        return if (this == Product) "دریافت مرجوعی" else "سایز بندی"
    }
}