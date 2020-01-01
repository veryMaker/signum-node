package brs.services

import brs.entity.Prop

interface PropertyService {
    /**
     * Get the value of the specified property, or the default value if it was not set
     * @param prop The property to get the value of
     * @return The value of [prop] or its default value if it is not set
     */
    fun <T : Any> get(prop: Prop<T>): T
}
