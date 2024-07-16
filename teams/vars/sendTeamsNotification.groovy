import groovy.json.JsonOutput

void call() {
    String webhookUrl = teamsWebhookUrl()

    Map<String, Object> payload = [
        'type'    : 'AdaptiveCard',
        'version' : '1.2',
        'body'    : [
            [
                'type'   : 'TextBlock',
                'text'   : 'Test Notification',
                'weight' : 'Bolder',
                'size'   : 'Medium'
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
