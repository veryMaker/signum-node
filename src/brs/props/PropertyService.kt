package brs.props

interface PropertyService {
    fun <T> get(prop: Prop<T>): T
    fun <T> get(propName: String, defaultValue: T): T
}
