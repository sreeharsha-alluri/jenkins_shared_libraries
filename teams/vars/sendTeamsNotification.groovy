import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl) {
    def webhookUrl = teamsWebhookUrl()
    def themeColor
    def activityTitle

    switch(status) {
        case "SUCCESS":
            themeColor = '007300'
            activityTitle = "Pipeline ${status}!"
            break
        case "FAILURE":
            themeColor = 'FF0000'
            activityTitle = "Pipeline ${status}!"
            break
        case "ABORTED":
            themeColor = '808080'
            activityTitle = "Pipeline ${status}!"
            break
        case "UNSTABLE":
            themeColor = 'FFA500'
            activityTitle = "Pipeline ${status}!"
            break
        default:
            themeColor = '000000'
            activityTitle = "Unknown Pipeline Status"
            break
    }

    def payload = [
        "@type": "MessageCard",
        "@context": "http://schema.org/extensions",
        "summary": "Pipeline ${status}",
        "themeColor": themeColor,
        "sections": [[
            "activityTitle": activityTitle,
            "facts": [
                ["name": "Status", "value": status],
                ["name": "Pipeline", "value": "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"]
            ]
        ]]
    ]

    def jsonPayload = JsonOutput.toJson(payload)

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
