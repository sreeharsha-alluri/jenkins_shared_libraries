import groovy.json.JsonOutput

void call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '',
          boolean onlyCustomMessage = false, String mergedPRsMessageTeams = '', String webhookUrl = '',
          List<Map<String, String>> mentions = []) {

    // If no mentions provided, get default mentions and tag
    if (!mentions) {
        mentions = userMentions()
    }
    
    String tag = mentionTag(mentions)

    String finalWebhookUrl = webhookUrl ?: teamsWebhookUrl()
    String icon = teamsIcon(status)
    String jobAndBuildNumber = "${jobName} #${buildNumber}"
    List<Map<String, Object>> bodyElements = []
    List<Map<String, Object>> mentionEntities = []

    String messageWithMentions = customMessage ? customMessage : tag

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

    if (messageWithMentions) {
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
