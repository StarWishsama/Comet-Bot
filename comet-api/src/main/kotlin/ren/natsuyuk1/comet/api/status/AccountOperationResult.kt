package ren.natsuyuk1.comet.api.status

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
enum class AccountOperationStatus(
    val statusCode: HttpStatusCode
) {
    OK(HttpStatusCode.OK),
    ALREADY_LOGON(HttpStatusCode.BadRequest),
    NOT_FOUND(HttpStatusCode.NotFound),
    LOGIN_FAILED(HttpStatusCode.BadRequest),
    INTERNAL_ERROR(HttpStatusCode.InternalServerError),
    NO_WRAPPER(HttpStatusCode.BadRequest),
}

@Serializable
data class AccountOperationResult(
    val status: AccountOperationStatus,
    val message: String
)
