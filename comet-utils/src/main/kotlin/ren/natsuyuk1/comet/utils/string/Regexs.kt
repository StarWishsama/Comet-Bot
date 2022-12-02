package ren.natsuyuk1.comet.utils.string

internal val colorRegex = Regex("\u001B\\[[;\\d]*m")
internal val numberRegex = Regex("[-+]?\\d*\\.?\\d+")
internal val alphabetNumberRegex = Regex("[a-zA-Z0-9]*")
