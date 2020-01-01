package brs.services

import brs.entity.FluxValue

interface FluxCapacitorService {
    /**
     * Get the value of [fluxValue] at the current blockchain height
     */
    fun <T> getValue(fluxValue: FluxValue<T>): T

    /**
     * Get the value of [fluxValue] at [height]
     */
    fun <T> getValue(fluxValue: FluxValue<T>, height: Int): T
}
