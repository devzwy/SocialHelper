package io.github.devzwy.socialhelper.alipay.data

/**
 * 支付宝授权回传参数
 */
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
