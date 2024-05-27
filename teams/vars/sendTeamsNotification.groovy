import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl, List<String> prsMerged = []) {
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

    def prsSection = ""
    if (prsMerged && prsMerged.size() > 0) {
        def prsList = prsMerged.collect { "<li>${it}</li>" }.join('')
        prsSection = """
        <h3>Merged PRs:</h3>
        <ul>${prsList}</ul>
        """
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
            ],
            "text": prsSection
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
