import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean includeDefaultMessages = true, String mergedPRsMessageTeams = '', boolean onlyCustomMessage = false) {
    String webhookUrl = teamsWebhookUrl()
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
        // Use \n for line breaks in the message
        String formattedCustomMessage = customMessage.replaceAll('```', '').replaceAll('```', '').replaceAll('\n', '\\\\n')
        bodyElements += [
            [
                'type': 'TextBlock',
                'text': formattedCustomMessage,
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

    String payloadJson = JsonOutput.toJson(payload)

    httpRequest httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                requestBody: payloadJson,
                url: webhookUrl
}
