import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean onlyCustomMessage = false, String mergedPRsMessageTeams = '', String webhookUrl = '', 
          boolean enableTagging = false) {

    // Uses the provided webhook URL or default if not provided
    String finalWebhookUrl = webhookUrl ?: teamsWebhookUrl()
    String icon = teamsIcon(status)
    String jobAndBuildNumber = "${jobName} #${buildNumber}"
    List<Map<String, Object>> bodyElements = []

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

    if (mergedPRsMessageTeams && !onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': mergedPRsMessageTeams,
                'wrap': true
            ]
        ]
    }

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
                    ],
                    'msteams': [
                        'width': 'Full'
                    ]
                ]
            ]
        ]
    ]

    // If tagging is enabled, add mention entities to the payload
    if (enableTagging) {
        def mentionEntities = createMentionEntities(['sreehass@amd.com', 'nikamuni@amd.com'])
        payload['attachments'][0]['content']['msteams']['entities'] = mentionEntities
    }

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
