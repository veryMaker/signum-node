package brs.props

interface PropertyService {
    fun <T: Any> get(prop: Prop<T>): T = get(prop.name, prop.defaultValue)
    fun <T: Any> get(propName: String, defaultValue: T): T
}
