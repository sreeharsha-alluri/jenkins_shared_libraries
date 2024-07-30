def formatPRLinks(String mergedPRs) {
    return mergedPRs.replaceAll(/<([^|]+)\|([^>]+)>/, "[$2]($1)")
}
