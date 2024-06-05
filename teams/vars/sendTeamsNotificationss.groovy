import groovy.json.JsonOutput

void call(String mention, List<String> mentionIds, List<String> mentionNames) {
    // Define other necessary variables
    def color = 'good'
    def webhookUrl = env.TEAMS_WEBHOOK_URL

    // Create the JSON payload
    def payload = """
    {
        "type": "message",
        "attachments": [
            {
                "contentType": "application/vnd.microsoft.card.adaptive",
                "content": {
                    "type": "AdaptiveCard",
                    "body": [
                        {
                            "type": "TextBlock",
                            "text": "${mention}",
                            "color": "${color}"
                        }
                    ],
                    "msteams": {
                        "entities": ${JsonOutput.toJson(createMentionEntities(mentionIds, mentionNames))}
                    }
                }
            }
        ]
    }
    """

    // Escape double quotes in the payload
    def escapedPayload = payload.replaceAll(/"/, '\\\\"')

    // Execute the curl command to send the payload to Microsoft Teams webhook
    try {
        sh """
        curl -X POST -H 'Content-Type: application/json' -d "${escapedPayload}" '${webhookUrl}'
        """
    } catch (Exception e) {
        // Handle the error
        echo "Error occurred while sending notification to Microsoft Teams: ${e.message}"
    }
}
