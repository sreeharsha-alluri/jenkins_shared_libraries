import groovy.json.JsonOutput

void call(String status, String pipelineName, int buildNumber, String buildUrl) {
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

    def prsMerged = getMergedPRs()
    def prList = prsMerged.collect { "- ${formatLink(it.url, it.title)} by ${it.author}" }.join('\n')

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
            "text": "PRs Merged to Nightly Branch:\n${prList}"
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

def getMergedPRs() {
    def mergedPRs = []
    def gitCmd = "git log --merges --oneline nightly_success..nightly"
    def process = gitCmd.execute()
    process.waitFor()

    process.text.eachLine { line ->
        def match = line =~ /Merge pull request #(\d+) from (.*)/
        if (match) {
            def prNumber = match[0][1]
            def author = match[0][2].trim()
            def prUrl = "https://github.com/nikhilkamuni/Teams_notification.git/pull/${prNumber}"
            def prTitle = sh(script: "git log --pretty=format:'%s' -n 1 $prNumber", returnStdout: true).trim()
            mergedPRs.add([number: prNumber, title: prTitle, url: prUrl, author: author])
        }
    }

    return mergedPRs
}
