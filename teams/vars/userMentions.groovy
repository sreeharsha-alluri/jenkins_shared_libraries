List<Map<String, String>> call() {
    // Run the Python script to get the usernames
    def userNames = sh(script: "python3 gheAutomation/resources/user_names.py", returnStdout: true).trim().split('\n')

    return userNames.collect { username ->
        [
            email: "${username}@amd.com",
            displayName: username
        ]
    }
}
