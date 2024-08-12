Map call(String email, String displayName) {
    return [
        'type': 'mention',
        'text': "<at>${displayName}</at>",
        'mentioned': [
            'id': email,
            'name': displayName
        ]
    ]
}
