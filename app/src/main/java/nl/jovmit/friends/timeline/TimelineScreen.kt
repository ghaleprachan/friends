package nl.jovmit.friends.timeline

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.jovmit.friends.R
import nl.jovmit.friends.domain.post.Post
import nl.jovmit.friends.timeline.state.TimelineState
import nl.jovmit.friends.ui.composables.BlockingLoading
import nl.jovmit.friends.ui.composables.InfoMessage
import nl.jovmit.friends.ui.composables.ScreenTitle

class TimelineScreenState(
  private val coroutineScope: CoroutineScope
) {
  var posts by mutableStateOf(emptyList<Post>())
  var loadedUserId by mutableStateOf("")
  var isLoading by mutableStateOf(false)
  var isInfoMessageShowing by mutableStateOf(false)
  var currentInfoMessage by mutableStateOf(0)

  fun updatePosts(newPosts: List<Post>) {
    isLoading = false
    this.posts = newPosts
  }

  fun shouldLoadPostsFor(userId: String): Boolean {
    if (loadedUserId != userId) {
      loadedUserId = userId
      return true
    }
    return false
  }

  fun showLoading() {
    isLoading = true
  }

  fun showInfoMessage(@StringRes infoMessage: Int) = coroutineScope.launch {
    isLoading = false
    if (currentInfoMessage != infoMessage) {
      currentInfoMessage = infoMessage
      if (!isInfoMessageShowing) {
        isInfoMessageShowing = true
        delay(1500)
        isInfoMessageShowing = false
      }
    }
  }
}

@Composable
fun TimelineScreen(
  userId: String,
  timelineViewModel: TimelineViewModel,
  onCreateNewPost: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val screenState by remember { mutableStateOf(TimelineScreenState(coroutineScope)) }
  val timelineState by timelineViewModel.timelineState.observeAsState()
  if (screenState.shouldLoadPostsFor(userId)) {
    timelineViewModel.timelineFor(userId)
  }

  when (timelineState) {
    is TimelineState.Loading -> screenState.showLoading()
    is TimelineState.Posts -> {
      val posts = (timelineState as TimelineState.Posts).posts
      screenState.updatePosts(posts)
    }
    is TimelineState.BackendError ->
      screenState.showInfoMessage(R.string.fetchingTimelineError)
    is TimelineState.OfflineError ->
      screenState.showInfoMessage(R.string.offlineError)
  }

  Box {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      ScreenTitle(resource = R.string.timeline)
      Spacer(modifier = Modifier.height(16.dp))
      Box(modifier = Modifier.fillMaxSize()) {
        PostsList(
          posts = screenState.posts,
          modifier = Modifier.align(Alignment.TopCenter)
        )
        FloatingActionButton(
          onClick = { onCreateNewPost() },
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .testTag(stringResource(id = R.string.createNewPost))
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.createNewPost)
          )
        }
      }
    }
    InfoMessage(
      isVisible = screenState.isInfoMessageShowing,
      stringResource = screenState.currentInfoMessage
    )
    BlockingLoading(isShowing = screenState.isLoading)
  }
}

@Composable
private fun PostsList(
  posts: List<Post>,
  modifier: Modifier = Modifier
) {
  if (posts.isEmpty()) {
    Text(
      text = stringResource(id = R.string.emptyTimelineMessage),
      modifier = modifier
    )
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(posts) { post ->
        PostItem(post = post)
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun PostItem(
  post: Post,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(shape = RoundedCornerShape(16.dp))
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = MaterialTheme.colors.onSurface,
        shape = RoundedCornerShape(16.dp)
      )
  ) {
    Text(
      text = post.text,
      modifier = Modifier.padding(16.dp)
    )
  }
}

@Preview
@Composable
private fun PostsListPreview() {
  val posts = (0..100).map { index ->
    Post("$index", "user$index", "This is a post number $index", index.toLong())
  }
  PostsList(posts)
}
