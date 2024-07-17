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
        facts.add(['title': 'Pipeline', 'value': "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"])
    }

    if (customMessage && !onlyCustomMessage) {
        facts.add(['title': '', 'value': teamsBold(customMessage)])
    }

    if (mergedPRsMessageTeams) {
        facts.add(['title': '', 'value': mergedPRsMessageTeams])
    }

    Map<String, Object> payload = [
        'type'       : 'AdaptiveCard',
        'context'    : 'https://schema.org/extensions',
        'themeColor' : themeColor,
        'version'    : '1.0',
        'body'       : [[
            'type': 'Container',
            'items': [[
                'type'   : 'TextBlock',
                'text'   : activityTitle,
                'weight' : 'bolder',
                'size'   : 'medium'
            ], [
                'type': 'FactSet',
                'facts': facts
            ]]
        ]]
    ]

    String jsonPayload = JsonOutput.toJson(payload)

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
