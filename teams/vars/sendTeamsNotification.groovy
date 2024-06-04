import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl, String prDetails) {
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
        ["name": "Pipeline", "value": "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"]
    ]

    def mentions = ''
    def mentionEntities = []

    if (prDetails) {
        def prLines = prDetails.split('<br>')
        prLines.each { line ->
            def matcher = line =~ /by ([^<]+)/
            if (matcher) {
                def username = matcher[0][1].trim()
                def email = "${username}@amd.com"
                mentions += "<at>${username}</at> "
                mentionEntities.add([
                    type: 'mention',
                    text: "<at>${username}</at>",
                    mentioned: [
                        id: email,
                        name: username
                    ]
                ])
            }
        }
        facts.add(["name": "Merged PRs", "value": prDetails])
    }

    def payload = [
        "@type": "MessageCard",
        "@context": "http://schema.org/extensions",
        "summary": "Pipeline ${status}",
        "themeColor": themeColor,
        "sections": [[
            "activityTitle": activityTitle,
            "facts": facts,
            "text": mentions
        ]],
        "potentialAction": [
            [
                "@type": "ActionCard",
                "name": "View Build",
                "inputs": [
                    [
                        "@type": "TextInput",
                        "id": "comment",
                        "isMultiline": true,
                        "title": "Comment"
                    ]
                ],
                "actions": [
                    [
                        "@type": "HttpPOST",
                        "name": "Send",
                        "target": webhookUrl
                    ]
                ]
            ]
        ],
        "msteams": [
            "entities": mentionEntities
        ]
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
