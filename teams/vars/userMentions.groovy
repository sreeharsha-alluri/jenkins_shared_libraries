List<Map<String, String>> call() {
    List<String> usernames = ['nikamuni']  // You can add more usernames here if needed
    return usernames.collect { username ->
        [
            email: "${username}@amd.com",
            displayName: username
        ]
    }
}
