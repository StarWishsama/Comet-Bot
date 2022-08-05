package ren.natsuyuk1.comet.network.thirdparty.bangumi.util

internal val startNumRegex by lazy { Regex("""^(\d+)""") }

internal val subjectIdRegex by lazy { Regex("""/subject/(\d+)""") }

internal val episodeIdRegex by lazy { Regex("""/ep/(\d+)""") }

internal val subjectRankRegex by lazy { Regex("""#(\d+)""") }

internal val subjectVoteRegex by lazy { Regex("""(\d+)人评分""") }

internal val searchSubjectId by lazy { Regex("""item_(\d+)""") }
