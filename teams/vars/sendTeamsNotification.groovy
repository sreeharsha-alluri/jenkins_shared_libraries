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
    def prIDs = getMergedPRIDs()
    
    prIDs.each { prID ->
        // Get the PR details from GitHub REST API as JSON
        def jsonResponse = sh(script: "curl -L --silent https://api.github.com/repos/nikhilkamuni/Teams_notification/pulls/${prID}", returnStdout: true)
        def prDetails = readJSON text: jsonResponse

        // Extract relevant information from the JSON response
        def prTitle = prDetails.title
        def prAuthor = prDetails.user.login
        def prUrl = prDetails.html_url

        // Add the PR details to the list
        mergedPRs.add([title: prTitle, author: prAuthor, url: prUrl])
    }

    return mergedPRs
}

def getMergedPRIDs() {
    def gitCmd = "git log --merges --oneline nightly_success..nightly"
    def process = gitCmd.execute()
    process.waitFor()

    def prIDs = []

    process.text.eachLine { line ->
        def match = line =~ /Merge pull request #(\d+)/
        if (match) {
            prIDs.add(match[0][1])
        }
    }

    return prIDs
}
