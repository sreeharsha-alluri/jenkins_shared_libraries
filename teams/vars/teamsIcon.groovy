String call(String reason) {
    String icon = ''
    switch (reason) {
        case 'SUCCESS':
            icon = '✔️'
            break
        case 'FAILURE':
            icon = '❌'
            break
        case 'ABORTED':
            icon = '🚫'
            break
        case 'UNSTABLE':
            icon = '⚠️'
            break
        case 'BULLET':
            icon = '•'
            break
    }
    return icon
}
