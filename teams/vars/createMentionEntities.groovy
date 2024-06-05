def call(List<String> mentionIds, List<String> mentionNames) {
    def entities = []
    mentionIds.eachWithIndex { id, index ->
        entities.add([
            type: 'mention',
            text: "<at>${id}</at>",
            mentioned: [
                id: id,
                name: mentionNames[index]
            ]
        ])
    }
    return entities
}
