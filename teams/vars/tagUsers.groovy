void call() {
    // Define user IDs and display names
    def userId1 = env.USER1_ID
    def userId2 = env.USER2_ID
    def displayName1 = env.USER1_DISPLAY_NAME
    def displayName2 = env.USER2_DISPLAY_NAME

    // Define users to tag based on certain conditions
    def mentionIds = [userId1, userId2]
    def mentionNames = [displayName1, displayName2]

    // Create mention dynamically using loop
    def mention = mentionIds.collect { "hello <at>${it}</at>" }.join(' ')

    // Call the function to send notification
    sendNotificationToTeams(mention, mentionIds, mentionNames)
}
