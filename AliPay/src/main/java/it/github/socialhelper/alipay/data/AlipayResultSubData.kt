package it.github.socialhelper.alipay.data

data class AlipayResultSubData(
    val success: Boolean,
    val result_code: Int,
    val app_id: String,
    val auth_code: String,
    val scope: String,
    val alipay_open_id: String,
    val user_id: String,
    val target_id: String
)
