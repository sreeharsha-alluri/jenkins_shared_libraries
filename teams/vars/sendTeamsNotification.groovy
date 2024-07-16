import groovy.json.JsonOutput

void call(
  String status,
  String pipelineName,
  int buildNumber,
  String buildUrl,
  String customMessage = '',
  boolean onlyCustomMessage = false,
  String mergedPRsMessageTeams = ''
  ) {
    String webhookUrl = teamsWebhookUrl()
    String themeColor
    String activityTitle
    String icon = teamsIcon(status)

    if (onlyCustomMessage) {
        themeColor = '008080'
        activityTitle = customMessage
    } else {
        switch (status) {
            case 'SUCCESS':
                themeColor = '007300'
                activityTitle = "${icon} Pipeline ${status}!"
                break
            case 'FAILURE':
                themeColor = 'FF0000'
                activityTitle = "${icon} Pipeline ${status}!"
                break
            case 'ABORTED':
                themeColor = '808080'
                activityTitle = "${icon} Pipeline ${status}!"
                break
            case 'UNSTABLE':
                themeColor = 'FFA500'
                activityTitle = "${icon} Pipeline ${status}!"
                break
            default:
                themeColor = '000000'
                activityTitle = "${icon} Unknown Pipeline Status"
                break
        }
    }

    List<Map<String, Object>> facts = []

    if (!onlyCustomMessage) {
        facts.add(['title': 'Pipeline', 'value': formatLink(buildUrl, "${pipelineName} #${buildNumber}")])
    }

    if (customMessage && !onlyCustomMessage) {
        facts.add(['title': '', 'value': teamsBold(customMessage)])
    }

    if (mergedPRsMessageTeams) {
        facts.add(['title': '', 'value': mergedPRsMessageTeams])
    }

    Map<String, Object> payload = [
        'type'       : 'AdaptiveCard',
        'version'    : '1.2',
        'body'       : [
            [
                'type'      : 'TextBlock',
                'text'      : activityTitle,
                'weight'    : 'Bolder',
                'size'      : 'Medium',
                'color'     : themeColor == 'FF0000' ? 'Attention' : themeColor == 'FFA500' ? 'Warning' : themeColor == '007300' ? 'Good' : 'Default'
            ],
            [
                'type'      : 'FactSet',
                'facts'     : facts
            ]
        ],
        'msteams'    : [
            'width'     : 'Full'
        ]
    ]

    String jsonPayload = JsonOutput.toJson(payload)

    // Log the payload for debugging
    echo "Payload: ${jsonPayload}"

    try {
        httpRequest(
            contentType: 'APPLICATION_JSON',
            httpMode: 'POST',
            requestBody: jsonPayload,
            url: webhookUrl
        )
    } catch (Exception e) {
        echo "Failed to send notification: ${e.message}"
    }
}
