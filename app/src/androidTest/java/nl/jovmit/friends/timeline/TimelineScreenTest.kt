package nl.jovmit.friends.timeline

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import nl.jovmit.friends.MainActivity
import nl.jovmit.friends.domain.post.InMemoryPostCatalog
import nl.jovmit.friends.domain.post.Post
import nl.jovmit.friends.domain.post.PostCatalog
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class TimelineScreenTest {

  @get:Rule
  val timelineTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun showsEmptyTimelineMessage() {
    val email = "lucy@friends.com"
    val password = "passPASS123#@"
    launchTimelineFor(email, password, timelineTestRule) {
      //no operation
    } verify {
      emptyTimelineMessageIsDisplayed()
    }
  }

  @Test
  fun showsAvailablePosts() {
    val email = "bob@friends.com"
    val password = "b0bPaS#2021"
    val post1 = Post("post1", "bobId", "This is Bob's first post", 1L)
    val post2 = Post("post2", "bobId", "Bob's second post is here!", 2L)
    replacePostCatalogWith(InMemoryPostCatalog(listOf(post1, post2)))

    launchTimelineFor(email, password, timelineTestRule) {
      //no operation
    } verify {
      postsAreDisplayed(post1, post2)
    }
  }

  @Test
  fun opensPostComposer() {
    launchTimelineFor("test@email.com", "sOmEPa$123", timelineTestRule) {
      tapOnCreateNewPost()
    } verify {
      newPostComposerIsDisplayed()
    }
  }

  @After
  fun tearDown() {
    replacePostCatalogWith(InMemoryPostCatalog())
  }

  private fun replacePostCatalogWith(postsCatalog: PostCatalog) {
    val replaceModule = module {
      factory(override = true) { postsCatalog }
    }
    loadKoinModules(replaceModule)
  }
}