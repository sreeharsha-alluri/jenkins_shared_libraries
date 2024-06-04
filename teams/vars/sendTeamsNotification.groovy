import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl, String prDetails, List<String> mentionedUsers, List<String> displayNames) {
    def webhookUrl = teamsWebhookUrl()
    def themeColor
    def activityTitle

    def icon = teamsIcon(status)

    switch(status) {
        case "SUCCESS":
            themeColor = '007300'
            activityTitle = "${icon} Pipeline ${status}!"
            break
        case "FAILURE":
            themeColor = 'FF0000'
            activityTitle = "${icon} Pipeline ${status}!"
            break
        case "ABORTED":
            themeColor = '808080'
            activityTitle = "${icon} Pipeline ${status}!"
            break
        case "UNSTABLE":
            themeColor = 'FFA500'
            activityTitle = "${icon} Pipeline ${status}!"
            break
        default:
            themeColor = '000000'
            activityTitle = "${icon} Unknown Pipeline Status"
            break
    }

    def facts = [
        ["name": "Status", "value": status],
        ["name": "Pipeline", "value": "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"]
    ]

    if (prDetails) {
        facts.add(["name": "Merged PRs", "value": prDetails])
    }

    def mentionEntities = createMentionEntities(mentionedUsers, displayNames)

    def payload = [
        "@type": "MessageCard",
        "@context": "http://schema.org/extensions",
        "summary": "Pipeline ${status}",
        "themeColor": themeColor,
        "sections": [[
            "activityTitle": activityTitle,
            "facts": facts,
            "msteams": [
                "entities": mentionEntities
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

def createMentionEntities(List<String> mentionedUsers, List<String> displayNames) {
    def entities = mentionedUsers.collectWithIndex { user, index ->
        [
            type: 'mention',
            text: "<at>${user}</at>",
            mentioned: [
                id: user,
                name: displayNames[index]
            ]
        ]
    }
    return entities
}
