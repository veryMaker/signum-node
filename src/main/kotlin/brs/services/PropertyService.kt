package brs.services

import brs.entity.Prop

interface PropertyService {
    /**
     * TODO
     */
    fun <T: Any> get(prop: Prop<T>): T = get(prop.name, prop.defaultValue)

    /**
     * TODO
     */
    fun <T: Any> get(propName: String, defaultValue: T): T
}
