package brs.util.ocl

import org.jocl.CL
import org.jocl.cl_mem

fun cl_mem.release() {
    CL.clReleaseMemObject(this)
}