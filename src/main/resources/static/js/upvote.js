// Handler for upvote clicks on posts
async function upvoteClickHandler(event) {
  event.preventDefault();

  // Grab the post id from the URL
  const id = window.location.toString().split('/')[
    window.location.toString().split('/').length - 1
  ];

  // fetch request to update the post's vote count by post id
  const response = await fetch('/posts/upvote', {
    method: 'PUT',
    body: JSON.stringify({
        postId: id
    }),
    headers: {
      'Content-Type': 'application/json'
    }
  });

  // Reload the page if database upvote successful
  // otherwise throw error alert
  if (response.ok) {
    document.location.reload();
  } else {
    alert(response.statusText);
  }
}

// Attach the upvote click handler function to the upvote button when clicked
document.querySelector('.upvote-btn').addEventListener('click', upvoteClickHandler);