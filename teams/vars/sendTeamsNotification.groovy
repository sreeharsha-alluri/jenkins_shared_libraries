import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean onlyCustomMessage = false, String mergedPRsMessageTeams = '', String webhookUrl = '', boolean tagUsers = false) {
    
    // Use the provided webhook URL or a default if not provided
    String finalWebhookUrl = webhookUrl ?: teamsWebhookUrl()
    String icon = teamsIcon(status)
    String jobAndBuildNumber = "${jobName} #${buildNumber}"
    List<Map<String, Object>> bodyElements = []
    List<Map<String, Object>> mentionEntities = []

    // Add basic notification content if onlyCustomMessage is false
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

    // Add custom message content if provided
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

    // Add merged PRs message if provided and onlyCustomMessage is false
    if (mergedPRsMessageTeams && !onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': mergedPRsMessageTeams,
                'wrap': true
            ]
        ]
    }

    // If tagging users is enabled, add mention entities for users
    if (tagUsers) {
        mentionEntities = createMentionEntities(['sreehass@amd.com', 'nikamuni@amd.com'])

        // Adding mentions to the notification body
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': "Hey <at>sreehass</at> and <at>nikamuni</at>, check the build status!",
                'wrap': true
            ]
        ]
    }

    // Build the payload structure
    Map<String, Object> payload = [
        'type': 'message',
        'attachments': [
            [
                'contentType': 'application/vnd.microsoft.card.adaptive',
                'content': [
                    'type': 'AdaptiveCard',
                    'version': '1.2',
                    'body': bodyElements,
                    'actions': onlyCustomMessage ? [] : [
                        [
                            'type': 'Action.OpenUrl',
                            'title': 'View Build',
                            'url': buildUrl
                        ]
                    ]
                ]
            ]
        ],
        'msteams': [
            'entities': mentionEntities,
            'width': 'Full'
        ]
    ]

    // Convert payload to JSON
    String payloadJson = JsonOutput.toJson(payload)

    // Log the payload for debugging purposes
    echo "Payload: ${payloadJson}"

    // Send the notification using httpRequest
    httpRequest httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                requestBody: payloadJson,
                url: finalWebhookUrl
}

// Function to create mention entities for the provided user IDs
List<Map<String, Object>> createMentionEntities(List<String> userIds) {
    List<Map<String, Object>> entities = []
    userIds.each { id ->
        def username = id.split('@')[0]  // Extract username from email
        entities.add([
            'type': 'mention',
            'text': "<at>${username}</at>",
            'mentioned': [
                'id': id,
                'name': username
            ]
        ])
    }
    return entities
}
