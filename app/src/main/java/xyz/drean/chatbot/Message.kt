package xyz.drean.chatbot

class Message {
    var message: String? = null
    var user: String? = null
    var time: String? = null

    constructor() {}

    constructor(message: String, user: String, time: String) {
        this.message = message
        this.user = user
        this.time = time
    }
}
