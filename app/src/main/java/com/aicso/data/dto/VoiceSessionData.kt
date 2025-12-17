package com.aicso.data.dto

import com.google.gson.annotations.SerializedName

data class VoiceSessionData(
    @SerializedName("exportId")
    val exportId: String,

    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("externalReferenceId")
    val externalReferenceId: String?,

    @SerializedName("errorMessage")
    val errorMessage: String?
)