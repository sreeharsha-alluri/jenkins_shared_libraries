import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean onlyCustomMessage = false, String mergedPRsMessageTeams = '', String webhookUrl = '') {

    // Uses the provided webhook URL or default if not provided
    String finalWebhookUrl = webhookUrl ?: teamsWebhookUrl()
    String icon = teamsIcon(status)
    String jobAndBuildNumber = "${jobName} #${buildNumber}"
    List<Map<String, Object>> bodyElements = []
    List<Map<String, Object>> mentionEntities = []

    // Temporary variable to hold the modified custom message
    String messageWithMentions = customMessage

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
                'text': messageWithMentions,
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

    // Determine who to mention dynamically
    List<Map<String, String>> mentions = getMentions()

    // Add mentions to the card if determined
    if (mentions) {
        mentions.each { mention ->
            Map<String, Object> mentionEntity = teamsMention(mention['email'], mention['displayName'])
            mentionEntities.add(mentionEntity)
            messageWithMentions = messageWithMentions.replace("<at>${mention['displayName']}</at>", mentionEntity['text'])
        }
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
                    'msteams': [
                        'width': 'Full',
                        'entities': mentionEntities
                    ],
                    'actions': onlyCustomMessage ? [] : [
                        [
                            'type': 'Action.OpenUrl',
                            'title': 'View Build',
                            'url': buildUrl
                        ]
                    ]
                ]
            ]
        ]
    ]

    String payloadJson = JsonOutput.toJson(payload)

    httpRequest httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                requestBody: payloadJson,
                url: finalWebhookUrl
}

// This method dynamically determines the person to be tagged (for example, PR owner)
List<Map<String, String>> getMentions() {
    // Logic to get user based on PR ownership, job status, etc.
    String prOwnerEmail = getPROwnerEmail()
    String prOwnerName = getPROwnerName()

    return [[email: prOwnerEmail, displayName: prOwnerName]]
}

String getPROwnerEmail() {
    // Extract PR owner email dynamically
    return "nikamuni@amd.com" // Example, replace with actual extraction logic
}

String getPROwnerName() {
    // Extract PR owner name dynamically
    return "nikamuni" // Example, replace with actual extraction logic
}
