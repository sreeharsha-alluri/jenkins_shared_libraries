void call(String status, String pipelineName, int buildNumber, String buildUrl, String branch) {
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

    def mergedPRs = getNewMergedPRs('nightly', 'nightly_success')

    def payload = [
        "@type": "MessageCard",
        "@context": "http://schema.org/extensions",
        "summary": "Pipeline ${status}",
        "themeColor": themeColor,
        "sections": [[
            "activityTitle": activityTitle,
            "facts": [
                ["name": "Status", "value": status],
                ["name": "Pipeline", "value": "<a href=\"$buildUrl\">${pipelineName} #${buildNumber}</a>"],
                ["name": "New Merged PRs", "value": mergedPRs]
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

String getNewMergedPRs(String baseBranch, String compareBranch) {
    def apiUrl = "https://api.github.com/repos/nikhilkamuni/Teams_notification/compare/${compareBranch}...${baseBranch}"
    def response = httpRequest(
        url: apiUrl,
        httpMode: 'GET',
        acceptType: 'APPLICATION_JSON'
    )

    def responseContent = response.content
    def compareResult = new groovy.json.JsonSlurperClassic().parseText(responseContent)
    def commits = compareResult.commits

    def mergedPRs = commits.collect { commit ->
        def prUrl = "https://api.github.com/repos/nikhilkamuni/Teams_notification/commits/${commit.sha}/pulls"
        def prResponse = httpRequest(
            url: prUrl,
            httpMode: 'GET',
            acceptType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Accept', value: 'application/vnd.github.groot-preview+json']]
        )
        def prs = new groovy.json.JsonSlurperClassic().parseText(prResponse.content)
        def pr = prs.find { it.merged_at != null }
        return pr ? "${pr.title} (#${pr.number}) by ${pr.user.login}" : null
    }.findAll { it != null }.join("\n")

    return mergedPRs ? mergedPRs : "No new PRs merged"
}
