package com.habit.app.http

import android.util.Log
import com.habit.app.data.TAG
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * 日志拦截器
 */
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.w(TAG, "➡️ Sending request: url:${request.url}")
//        Log.w(TAG, "    request header: ${request.headers}")
        // 打印请求体参数
//        printRequestParameters(request)
        val response = chain.proceed(request)
        val responseBody = response.body
        val responseBodyStr = response.body?.string() ?: ""
//        Log.w(TAG, "⬅️ Received response: url：${request.url}, body:$responseBodyStr")
        Log.w(TAG, "⬅️ Received response: url：${request.url}")
        return response.newBuilder().body(responseBodyStr.toResponseBody(responseBody?.contentType())).build()
    }

    private fun printRequestParameters(request: Request) {
        when (request.method) {
            "GET", "DELETE" -> {
                // 打印URL参数
                val queryParams = request.url.queryParameterNames
                if (queryParams.isNotEmpty()) {
                    Log.w(TAG, "    URL Parameters:")
                    queryParams.forEach { key ->
                        request.url.queryParameterValues(key).forEach { value ->
                            Log.w(TAG, "    $key=$value")
                        }
                    }
                } else {
                    Log.w(TAG, "    Parameters: None")
                }
            }
            "POST", "PUT", "PATCH" -> {
                // 打印请求体参数
                val requestBody = request.body
                if (requestBody != null) {
                    when (requestBody) {
                        is FormBody -> {
                            Log.w(TAG, "    Form Parameters:")
                            for (i in 0 until requestBody.size) {
                                Log.w(TAG, "    ${requestBody.name(i)}=${requestBody.value(i)}")
                            }
                        }
                        is MultipartBody -> {
                            Log.w(TAG, "    Multipart Parameters (${requestBody.parts.size} parts)")
                        }
                        else -> {
                            // 打印JSON等请求体内容
                            try {
                                val buffer = Buffer()
                                requestBody.writeTo(buffer)
                                val bodyString = buffer.readUtf8()
                                Log.w(TAG, "    Request Body: $bodyString")
                            } catch (e: Exception) {
                                Log.w(TAG, "    Request Body: [Unable to read]")
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "    Request Body: Empty")
                }
            }
        }
    }
}

/**
 * Header 拦截器（例如统一加 token）
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newRequest = original.newBuilder()
            .header("Content-Type", "application/json")
            .build()
        return chain.proceed(newRequest)
    }
}

/**
 * 回调结果拦截器
 */
class ResultInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val bodyString = response.body?.string() ?: ""
        // 统一处理 code
        // val json = JSONObject(bodyString)
        // val code = json.optInt("code", 0)

        val newBody = bodyString.toResponseBody(response.body?.contentType())
        return response.newBuilder().body(newBody).build()
    }
}
