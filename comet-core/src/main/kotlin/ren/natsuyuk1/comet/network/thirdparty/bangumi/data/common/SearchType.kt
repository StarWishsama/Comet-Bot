package ren.natsuyuk1.comet.network.thirdparty.bangumi.data.common

sealed interface SearchType {
    val category: String
    val name: String

    sealed interface Subject : SearchType {
        object All : Subject {
            override val category = "all"
            override val name: String = "条目"
        }

        object Anime : Subject {
            override val category = "2"
            override val name: String = "动画"
        }

        object Book : Subject {
            override val category = "1"
            override val name: String = "书籍"
        }

        object Music : Subject {
            override val category = "3"
            override val name: String = "音乐"
        }

        object Game : Subject {
            override val category = "4"
            override val name: String = "游戏"
        }

        object Real : Subject {
            override val category = "6"
            override val name: String = "三次元"
        }
    }

    sealed interface Person : SearchType {
        object Character : Person {
            override val category: String = "crt"
            override val name: String = "虚拟人物"
        }

        object Real : Person {
            override val category: String = "prsn"
            override val name: String = "现实人物"
        }

        object All : Person {
            override val category = "all"
            override val name: String = "人物"
        }
    }
}
