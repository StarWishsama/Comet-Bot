package ren.natsuyuk1.comet.objects.github.data

enum class SecretStatus {

    /**
     * 找不到任何使用 Secret 的项目, 可视作成功
     */
    FOUND,

    /**
     * 已找到对应 Secret 的项目, 可视作成功
     */
    FOUND_WITH_SECRET,

    /**
     * 已找到对应 Secret 的项目, 但无法匹配
     */
    UNAUTHORIZED,

    /**
     * 找不到请求的项目
     */
    NOT_FOUND,

    /**
     * Comet 不支持的事件
     */
    UNSUPPORTED_EVENT
}
