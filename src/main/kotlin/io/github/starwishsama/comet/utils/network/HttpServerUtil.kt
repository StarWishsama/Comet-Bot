/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils.network

import cn.hutool.http.HttpStatus
import com.sun.net.httpserver.HttpExchange
import java.nio.charset.Charset

fun HttpExchange.writeTextResponse(
    text: String,
    charset: Charset = Charsets.UTF_8,
    statusCode: Int = HttpStatus.HTTP_OK
) {
    val bytes = text.toByteArray(charset)
    sendResponseHeaders(statusCode, bytes.size.toLong())

    responseBody.use {
        it.write(bytes)
        it.flush()
    }
}
