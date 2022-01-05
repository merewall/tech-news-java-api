// Function to handle deleting a post
async function deleteFormHandler(event) {
  event.preventDefault();

  // Grab the post id from the URL
  const id = window.location.toString().split('/')[
    window.location.toString().split('/').length - 1
  ];

  // Fetch request to delete the post by id
  const response = await fetch(`/api/posts/${id}`, {
    method: 'DELETE'
  });

  // If deletion is successful, redirect to dashboard
  // otherwise, alert error
  if (response.ok) {
    document.location.replace('/dashboard/')
  } else {
    alert(response.statusText);
  }
}

// Attach the delete handling function to the Delete Post button upon click
document.querySelector('.delete-post-btn').addEventListener('click', deleteFormHandler);