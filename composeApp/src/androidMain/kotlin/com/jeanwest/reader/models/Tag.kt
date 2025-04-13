package com.jeanwest.reader.models

import com.rscja.deviceapi.entity.UHFTAGInfo

data class Tag(
    val tagInfo: UHFTAGInfo,
    val epcDetails: EPC?,
    val status: TagStatus,
)

enum class TagStatus {
    RAW,
    CORRECT,
    WRONG_DATA,
    DUPLICATE;

    override fun toString(): String {

        return when (this) {
            RAW -> "خام"
            CORRECT -> "درست"
            WRONG_DATA -> "رایت اشتباه"
            DUPLICATE -> "رایت تکراری"
        }
    }
}

