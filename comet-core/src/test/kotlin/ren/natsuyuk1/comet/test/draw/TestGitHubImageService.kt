package ren.natsuyuk1.comet.test.draw

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.service.GitHubService
import ren.natsuyuk1.comet.service.image.GitHubImageService
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGitHubImageService {
    @BeforeAll
    fun init() {
        if (isCI()) return

        runBlocking {
            SkikoHelper.findSkikoLibrary()
        }
    }

    @Test
    fun test() {
      if (isCI()) return
      
        val payload = """{
  "ref": "refs/heads/dev",
  "before": "624891b46da95a3c69d93349e643a6a5cc20a39a",
  "after": "b1b055a4a89a0971c861c9ec35fd912ac725066d",
  "repository": {
    "id": 173288057,
    "node_id": "MDEwOlJlcG9zaXRvcnkxNzMyODgwNTc=",
    "name": "Comet-Bot",
    "full_name": "StarWishsama/Comet-Bot",
    "private": false,
    "owner": {
      "name": "StarWishsama",
      "email": "starwishsama@outlook.com",
      "login": "StarWishsama",
      "id": 25561848,
      "node_id": "MDQ6VXNlcjI1NTYxODQ4",
      "avatar_url": "https://avatars.githubusercontent.com/u/25561848?v=4",
      "gravatar_id": "",
      "url": "https://api.github.com/users/StarWishsama",
      "html_url": "https://github.com/StarWishsama",
      "followers_url": "https://api.github.com/users/StarWishsama/followers",
      "following_url": "https://api.github.com/users/StarWishsama/following{/other_user}",
      "gists_url": "https://api.github.com/users/StarWishsama/gists{/gist_id}",
      "starred_url": "https://api.github.com/users/StarWishsama/starred{/owner}{/repo}",
      "subscriptions_url": "https://api.github.com/users/StarWishsama/subscriptions",
      "organizations_url": "https://api.github.com/users/StarWishsama/orgs",
      "repos_url": "https://api.github.com/users/StarWishsama/repos",
      "events_url": "https://api.github.com/users/StarWishsama/events{/privacy}",
      "received_events_url": "https://api.github.com/users/StarWishsama/received_events",
      "type": "User",
      "site_admin": false
    },
    "html_url": "https://github.com/StarWishsama/Comet-Bot",
    "description": "☄ 多平台动态推送, 抽卡模拟器, 以及更多",
    "fork": false,
    "url": "https://github.com/StarWishsama/Comet-Bot",
    "forks_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/forks",
    "keys_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/keys{/key_id}",
    "collaborators_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/collaborators{/collaborator}",
    "teams_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/teams",
    "hooks_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/hooks",
    "issue_events_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/issues/events{/number}",
    "events_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/events",
    "assignees_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/assignees{/user}",
    "branches_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/branches{/branch}",
    "tags_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/tags",
    "blobs_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/git/blobs{/sha}",
    "git_tags_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/git/tags{/sha}",
    "git_refs_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/git/refs{/sha}",
    "trees_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/git/trees{/sha}",
    "statuses_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/statuses/{sha}",
    "languages_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/languages",
    "stargazers_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/stargazers",
    "contributors_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/contributors",
    "subscribers_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/subscribers",
    "subscription_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/subscription",
    "commits_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/commits{/sha}",
    "git_commits_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/git/commits{/sha}",
    "comments_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/comments{/number}",
    "issue_comment_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/issues/comments{/number}",
    "contents_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/contents/{+path}",
    "compare_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/compare/{base}...{head}",
    "merges_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/merges",
    "archive_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/{archive_format}{/ref}",
    "downloads_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/downloads",
    "issues_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/issues{/number}",
    "pulls_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/pulls{/number}",
    "milestones_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/milestones{/number}",
    "notifications_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/notifications{?since,all,participating}",
    "labels_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/labels{/name}",
    "releases_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/releases{/id}",
    "deployments_url": "https://api.github.com/repos/StarWishsama/Comet-Bot/deployments",
    "created_at": 1551438375,
    "updated_at": "2022-09-13T05:00:38Z",
    "pushed_at": 1665244372,
    "git_url": "git://github.com/StarWishsama/Comet-Bot.git",
    "ssh_url": "git@github.com:StarWishsama/Comet-Bot.git",
    "clone_url": "https://github.com/StarWishsama/Comet-Bot.git",
    "svn_url": "https://github.com/StarWishsama/Comet-Bot",
    "homepage": "",
    "size": 40066,
    "stargazers_count": 189,
    "watchers_count": 189,
    "language": "Kotlin",
    "has_issues": true,
    "has_projects": true,
    "has_downloads": true,
    "has_wiki": true,
    "has_pages": false,
    "forks_count": 18,
    "mirror_url": null,
    "archived": false,
    "disabled": false,
    "open_issues_count": 8,
    "license": {
      "key": "mit",
      "name": "MIT License",
      "spdx_id": "MIT",
      "url": "https://api.github.com/licenses/mit",
      "node_id": "MDc6TGljZW5zZTEz"
    },
    "allow_forking": true,
    "is_template": false,
    "web_commit_signoff_required": false,
    "topics": [
      "arknights",
      "bilibili",
      "kotlin",
      "mirai",
      "mirai-bot",
      "mirai-plugin",
      "projectsekai",
      "qq-bot",
      "qqbot",
      "rcon",
      "saucenao",
      "telegram-bot",
      "twitter-bot"
    ],
    "visibility": "public",
    "forks": 18,
    "open_issues": 8,
    "watchers": 189,
    "default_branch": "dev",
    "stargazers": 189,
    "master_branch": "dev"
  },
  "pusher": {
    "name": "StarWishsama",
    "email": "starwishsama@outlook.com"
  },
  "sender": {
    "login": "StarWishsama",
    "id": 25561848,
    "node_id": "MDQ6VXNlcjI1NTYxODQ4",
    "avatar_url": "https://avatars.githubusercontent.com/u/25561848?v=4",
    "gravatar_id": "",
    "url": "https://api.github.com/users/StarWishsama",
    "html_url": "https://github.com/StarWishsama",
    "followers_url": "https://api.github.com/users/StarWishsama/followers",
    "following_url": "https://api.github.com/users/StarWishsama/following{/other_user}",
    "gists_url": "https://api.github.com/users/StarWishsama/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/StarWishsama/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/StarWishsama/subscriptions",
    "organizations_url": "https://api.github.com/users/StarWishsama/orgs",
    "repos_url": "https://api.github.com/users/StarWishsama/repos",
    "events_url": "https://api.github.com/users/StarWishsama/events{/privacy}",
    "received_events_url": "https://api.github.com/users/StarWishsama/received_events",
    "type": "User",
    "site_admin": false
  },
  "created": false,
  "deleted": false,
  "forced": false,
  "base_ref": null,
  "compare": "https://github.com/StarWishsama/Comet-Bot/compare/624891b46da9...b1b055a4a89a",
  "commits": [
    {
      "id": "b1b055a4a89a0971c861c9ec35fd912ac725066d",
      "tree_id": "dadbc586f21cda219d1a04d435a176f0096b1dd6",
      "distinct": true,
      "message": ":bug: fix(telegram): don't expose telegram token in url",
      "timestamp": "2022-10-08T23:52:39+08:00",
      "url": "https://github.com/StarWishsama/Comet-Bot/commit/b1b055a4a89a0971c861c9ec35fd912ac725066d",
      "author": {
        "name": "StarWishsama",
        "email": "starwishsama@outlook.com",
        "username": "StarWishsama"
      },
      "committer": {
        "name": "StarWishsama",
        "email": "starwishsama@outlook.com",
        "username": "StarWishsama"
      },
      "added": [

      ],
      "removed": [

      ],
      "modified": [
        "comet-core/src/main/kotlin/ren/natsuyuk1/comet/commands/service/BiliBiliService.kt",
        "comet-telegram-wrapper/src/main/kotlin/ren/natsuyuk1/comet/telegram/util/MessageWrapper.kt"
      ]
    }
  ],
  "head_commit": {
    "id": "b1b055a4a89a0971c861c9ec35fd912ac725066d",
    "tree_id": "dadbc586f21cda219d1a04d435a176f0096b1dd6",
    "distinct": true,
    "message": ":bug: fix(telegram): don't expose telegram token in url",
    "timestamp": "2022-10-08T23:52:39+08:00",
    "url": "https://github.com/StarWishsama/Comet-Bot/commit/b1b055a4a89a0971c861c9ec35fd912ac725066d",
    "author": {
      "name": "StarWishsama",
      "email": "starwishsama@outlook.com",
      "username": "StarWishsama"
    },
    "committer": {
      "name": "StarWishsama",
      "email": "starwishsama@outlook.com",
      "username": "StarWishsama"
    },
    "added": [

    ],
    "removed": [

    ],
    "modified": [
      "comet-core/src/main/kotlin/ren/natsuyuk1/comet/commands/service/BiliBiliService.kt",
      "comet-telegram-wrapper/src/main/kotlin/ren/natsuyuk1/comet/telegram/util/MessageWrapper.kt"
    ]
  }
}"""
        val event = GitHubService.processEvent(payload, "push") ?: error("GitHub event parse failed")
        val output = GitHubImageService.drawEventInfo(event)
        println(output?.absPath)
        require(output != null && output.exists())
    }
}
