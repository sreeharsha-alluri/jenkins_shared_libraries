import groovy.json.JsonOutput

def call() {
    // Define user IDs and display names
    def userId1 = env.USER1_ID
    def userId2 = env.USER2_ID
    def displayName1 = env.USER1_DISPLAY_NAME
    def displayName2 = env.USER2_DISPLAY_NAME

    // Define users to tag based on certain conditions
    def mentionIds = [userId1, userId2]
    def mentionNames = [displayName1, displayName2]

    // Create mention dynamically using loop
    def mention = mentionNames.collect { "hello <at>${it}</at>" }.join(' ')

    // Call the function to send notification
    sendNotificationToTeams(mention, mentionIds, mentionNames)
}

def sendNotificationToTeams(String mention, List<String> mentionIds, List<String> mentionNames) {
    // Define other necessary variables
    def color = 'good'
    def webhookUrl = env.TEAMS_WEBHOOK_URL

    // Create the JSON payload
    def payload = [
        type: "message",
        attachments: [
            [
                contentType: "application/vnd.microsoft.card.adaptive",
                content: [
                    type: "AdaptiveCard",
                    body: [
                        [
                            type: "TextBlock",
                            text: mention,
                            color: color
                        ]
                    ],
                    msteams: [
                        entities: createMentionEntities(mentionIds, mentionNames)
                    ]
                ]
            ]
        ]
    ]

    def jsonPayload = JsonOutput.toJson(payload)

    // Execute the curl command to send the payload to Microsoft Teams webhook
    try {
        sh """
        curl -X POST -H 'Content-Type: application/json' -d '${jsonPayload}' '${webhookUrl}'
        """
    } catch (Exception e) {
        // Handle the error
        echo "Error occurred while sending notification to Microsoft Teams: ${e.message}"
    }
}

def createMentionEntities(List<String> mentionIds, List<String> mentionNames) {
    def entities = []
    mentionIds.eachWithIndex { id, index ->
        entities.add([
            type: 'mention',
            text: "<at>${mentionNames[index]}</at>",
            mentioned: [
                id: id,
                name: mentionNames[index]
            ]
        ])
    }
    return entities
}
