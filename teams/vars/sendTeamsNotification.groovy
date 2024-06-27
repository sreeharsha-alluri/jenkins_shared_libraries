import groovy.json.JsonOutput

void call(
  String status,
  String pipelineName,
  int buildNumber,
  String buildUrl,
  String customMessage = '',
  boolean onlyCustomMessage = false,
  String mergedPRsMessage = ''
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

    List<Map<String, String>> facts = []

    if (!onlyCustomMessage) {
        println "buildUrl: ${buildUrl}"
        println "<a href=\"$buildUrl\">testing_hyperlink sdf sd</a>"
        String message_string=formatLink(buildUrl, "${pipelineName} #${buildNumber}).toString()
        facts.add(['name': 'Pipeline', 'value': formatLink(buildUrl, "${pipelineName} #${buildNumber}").toString()])
        facts.add(['name': 'Pipeline', 'value': message_string)])
        facts.add(['name': 'Pipeline', 'value': "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"])
    }

    if (customMessage && !onlyCustomMessage) {
        facts.add(['name': '', 'value': teamsBold(customMessage)])
    }

    if (mergedPRsMessage) {
        facts.add(['name': '', 'value': mergedPRsMessage])
    }

    Map<String, Object> payload = [
        '@type'      : 'MessageCard',
        '@context'   : 'http://schema.org/extensions',
        'summary'    : onlyCustomMessage ? customMessage : "Pipeline ${status}",
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
