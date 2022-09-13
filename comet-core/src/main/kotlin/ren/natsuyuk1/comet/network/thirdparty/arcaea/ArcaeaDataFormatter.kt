package ren.natsuyuk1.comet.network.thirdparty.arcaea

internal fun Int.formatType(): String =
    when (this) {
        // 4 简单型角色, 5 困难型角色
        0 -> "FAILED"
        1 -> "Clear"
        4 -> "Easy Clear"
        5 -> "Hard Clear"
        2 -> "Full Recall"
        3 -> "Pure Memory"
        else -> "不存在捏 ($this)"
    }

internal fun Int.formatDifficulty(): String =
    when (this) {
        1 -> "PST"
        2 -> "PRS"
        3 -> "FTR"
        4 -> "BYD"
        else -> "不存在捏 ($this)"
    }

internal fun Int.formatScore(): String =
    when {
        this in 8600000..8899999 -> "C"
        this in 8900000..9199999 -> "B"
        this in 9200000..9499999 -> "A"
        this in 9500000..9799999 -> "AA"
        this in 9800000..9899999 -> "EX"
        this >= 9900000 -> "EX+"
        this in 0..8599999 -> "D"
        else -> "不存在捏"
    }
