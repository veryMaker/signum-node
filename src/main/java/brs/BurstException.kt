package brs

abstract class BurstException : Exception {

    protected constructor() : super()

    internal constructor(message: String) : super(message)

    internal constructor(message: String, cause: Throwable) : super(message, cause)

    protected constructor(cause: Throwable) : super(cause)

    abstract class ValidationException : BurstException {
        internal constructor(message: String) : super(message)

        internal constructor(message: String, cause: Throwable) : super(message, cause)
    }

    open class NotCurrentlyValidException : ValidationException {
        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class NotYetEnabledException : NotCurrentlyValidException {
        constructor(message: String) : super(message)

        constructor(message: String, throwable: Throwable) : super(message, throwable)
    }

    class NotValidException : ValidationException {
        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class StopException : RuntimeException {

        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)

    }
}
