import groovy.json.JsonOutput

def call(String status, String jobName, int buildNumber, String buildUrl, String customMessage = '', boolean includeDefaultMessages = true, String mergedPRsMessageTeams = '') {
    def webhookUrl = teamsWebhookUrl()

    def icon = teamsIcon(status)
    def jobAndBuildNumber = "${jobName} #${buildNumber}"
    def boldStatus = teamsBold(status)

    def bodyElements = [
        [
            'type': 'TextBlock',
            'size': 'Large',
            'weight': 'Bolder',
            'text': "${icon} ${jobAndBuildNumber}",
            'wrap': true
        ]
    ]

    if (includeDefaultMessages) {
        bodyElements += [
            [
                'type': 'FactSet',
                'facts': [
                    [
                        'title': 'Job:',
                        'value': jobAndBuildNumber
                    ],
                    [
                        'title': 'Build Number:',
                        'value': "${buildNumber}"
                    ],
                    [
                        'title': 'Status:',
                        'value': boldStatus
                    ]
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

    if (mergedPRsMessageTeams) {
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
                    'actions': [
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
