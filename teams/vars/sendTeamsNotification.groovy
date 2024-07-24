import groovy.json.JsonOutput

def call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '', boolean includeDefaultMessages = true, String mergedPRsMessageTeams = '', boolean onlyCustomMessage = false) {
    def webhookUrl = teamsWebhookUrl()

    def icon = teamsIcon(status)
    def jobAndBuildNumber = "${jobName} #${buildNumber}"
    def boldStatus = teamsBold(status)

    def bodyElements = []

    if (!onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'TextBlock',
                'size': 'Large',
                'weight': 'Bolder',
                'text': "${icon} ${jobAndBuildNumber}",
                'wrap': true
            ]
        ]
    }

    if (includeDefaultMessages && !onlyCustomMessage) {
        bodyElements += [
            [
                'type': 'FactSet',
                'facts': [
                ]
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

    def payload = [
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

    def payloadJson = JsonOutput.toJson(payload)

    httpRequest httpMode: 'POST',
                contentType: 'APPLICATION_JSON',
                requestBody: payloadJson,
                url: webhookUrl
}
