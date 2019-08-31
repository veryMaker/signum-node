package brs.at

class AtException : Exception {

    constructor(message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}

    companion object {
        private val serialVersionUID = 1L
    }
}
