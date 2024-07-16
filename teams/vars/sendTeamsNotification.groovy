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

    switch (status) {
        case 'SUCCESS':
            themeColor = '007300'
            activityTitle = "Pipeline SUCCESS!"
            break
        case 'FAILURE':
            themeColor = 'FF0000'
            activityTitle = "Pipeline FAILURE!"
            break
        case 'ABORTED':
            themeColor = '808080'
            activityTitle = "Pipeline ABORTED!"
            break
        case 'UNSTABLE':
            themeColor = 'FFA500'
            activityTitle = "Pipeline UNSTABLE!"
            break
        default:
            themeColor = '000000'
            activityTitle = "Unknown Pipeline Status"
            break
    }

    List<Map<String, String>> facts = []

    if (!onlyCustomMessage) {
        facts.add(['title': 'Pipeline', 'value': "${pipelineName} #${buildNumber}"])
    }

    if (customMessage && !onlyCustomMessage) {
        facts.add(['title': 'Message', 'value': customMessage])
    }

    if (mergedPRsMessageTeams) {
        facts.add(['title': 'Merged PRs', 'value': mergedPRsMessageTeams])
    }

    Map<String, Object> payload = [
        'type'    : 'AdaptiveCard',
        'version' : '1.2',
        'body'    : [
            [
                'type'   : 'TextBlock',
                'text'   : activityTitle,
                'weight' : 'Bolder',
                'size'   : 'Medium',
                'color'  : themeColor == 'FF0000' ? 'Attention' : themeColor == 'FFA500' ? 'Warning' : themeColor == '007300' ? 'Good' : 'Default'
            ],
            [
                'type' : 'FactSet',
                'facts': facts
            ]
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
