package brs.services

import brs.entity.FluxEnable
import brs.entity.FluxValue

interface FluxCapacitorService {

    /**
     * TODO
     */
    fun <T> getValue(fluxValue: FluxValue<T>): T

    /**
     * TODO
     */
    fun <T> getValue(fluxValue: FluxValue<T>, height: Int): T

    /**
     * TODO
     */
    fun getStartingHeight(fluxEnable: FluxEnable): Int?
}
