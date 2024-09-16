import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean onlyCustomMessage = false, String mergedPRsMessageTeams = '', String webhookUrl = '') {
    
    // Uses the provided webhook URL or default if not provided
    String finalWebhookUrl = webhookUrl ?: teamsWebhookUrl()
    String icon = teamsIcon(status)
    String jobAndBuildNumber = "${jobName} #${buildNumber}"
    List<Map<String, Object>> bodyElements = []

    // Tagging Users
    def mentionEntities = createMentionEntities(['sreehass@amd.com', 'nikamuni@amd.com'])

    // Add job and status information to the message body
    if (!onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'size': 'Large',
                'weight': 'Bolder',
                'text': "${icon} ${jobAndBuildNumber} ${status}",
                'wrap': true
            ]
        ]
    }

    // Add custom message if provided
    if (customMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': customMessage,
                'weight': 'Bolder',
                'wrap': true
            ]
        ]
    }

    // Add merged PRs message if provided
    if (mergedPRsMessageTeams && !onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': mergedPRsMessageTeams,
                'wrap': true
            ]
        ]
    }

    // Create the payload with mention entities
    Map<String, Object> payload = [
        'type': 'message',
        'attachments': [
            [
                'contentType': 'application/vnd.microsoft.card.adaptive',
                'content': [
                    'type': 'AdaptiveCard',
                    'version': '1.2',
                    'body': bodyElements,
                    'msteams': [
                        'entities': mentionEntities,
                        'width': 'Full'
                    ]
                ]
            ]
        ]
    ]

    // Convert the payload to JSON
    String payloadJson = JsonOutput.toJson(payload)

    // Debugging: Print the payload
    echo "Payload: ${payloadJson}"

    // Send the notification to the Teams webhook
    httpRequest httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                requestBody: payloadJson,
                url: finalWebhookUrl
}

// Function to create mention entities dynamically
List<Map> createMentionEntities(List<String> emails) {
    List<Map> entities = []
    emails.each { email ->
        def displayName = email.split('@')[0] // Extract display name from email
        entities.add([
            'type': 'mention',
            'text': "<at>${displayName}</at>",
            'mentioned': [
                'id': email,
                'name': displayName
            ]
        ])
    }
    return entities
}
