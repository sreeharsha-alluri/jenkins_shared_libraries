import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl, String customMessage = "") {
    String webhookUrl = teamsWebhookUrl()
    String themeColor
    String activityTitle

    String icon = teamsIcon(status)

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

    List<Map<String, String>> facts = [
        ['name': 'Pipeline', 'value': "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"]
    ]

    if (customMessage) {
        facts.add(['name': '', 'value': customMessage])
    }

    Map<String, Object> payload = [
        '@type'      : 'MessageCard',
        '@context'   : 'http://schema.org/extensions',
        'summary'    : "Pipeline ${status}",
        'themeColor' : themeColor,
        'sections'   : [[
            'activityTitle': activityTitle,
            'facts'        : facts
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
