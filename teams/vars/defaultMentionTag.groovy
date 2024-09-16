String call(List<Map<String, String>> mentions) {
    String tag = ''
    mentions.each { mention ->
        tag += "<at>${mention['displayName']}</at> "
    }
    return tag.trim() + " for testing"
}
