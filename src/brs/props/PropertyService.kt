package brs.props

interface PropertyService {
    fun <T> get(prop: Prop<T>): T
}
