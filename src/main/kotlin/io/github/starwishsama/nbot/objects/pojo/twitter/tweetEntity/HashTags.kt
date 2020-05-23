package io.github.starwishsama.nbot.objects.pojo.twitter.tweetEntity

data class HashTags(
    /**
     * An array of integers indicating the offsets within the Tweet text where the hashtag begins and ends.
     * The first integer represents the location of the # character in the Tweet text string.
     * The second integer represents the location of the first character after the hashtag.
     * Therefore the difference between the two numbers will be the length of the hashtag name plus one
     * (for the ‘#’ character).
     */
    val indices: List<Int>,
    /**
     * hashtag 的名字, 删去了开头的 ‘#’
     */
    val text: String)