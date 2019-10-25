package brs.services

import brs.entity.FluxEnable
import brs.entity.FluxValue

interface FluxCapacitorService {
    fun <T> getValue(fluxValue: FluxValue<T>): T
    fun <T> getValue(fluxValue: FluxValue<T>, height: Int): T
    fun getStartingHeight(fluxEnable: FluxEnable): Int?
}
