import groovy.json.JsonOutput

def call(String status, String jobName, int buildNumber, String buildUrl) {
    def webhookUrl = teamsWebhookUrl()

    def icon = teamsIcon(status)
    def boldJobName = teamsBold(jobName)
    def boldBuildNumber = teamsBold(buildNumber.toString())
    def boldStatus = teamsBold(status)
    def hyperlink = "<a href=\"$buildUrl\"><b>${jobName} #${buildNumber}</b></a>"

    def payload = [
        'type': 'message',
        'attachments': [
            [
                'contentType': 'application/vnd.microsoft.card.adaptive',
                'content': [
                    'type': 'AdaptiveCard',
                    'version': '1.2',
                    'body': [
                        [
                            'type': 'TextBlock',
                            'size': 'Large',
                            'weight': 'Bolder',
                            'text': "${icon} ${hyperlink}",
                            'wrap': true
                        ],
                        [
                            'type': 'FactSet',
                            'facts': [
                                [
                                    'title': 'Job:',
                                    'value': hyperlink
                                ],
                                [
                                    'title': 'Build Number:',
                                    'value': boldBuildNumber
                                ],
                                [
                                    'title': 'Status:',
                                    'value': boldStatus
                                ]
                            ]
                        ]
                    ],
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
