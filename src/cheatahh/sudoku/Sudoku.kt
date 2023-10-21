package cheatahh.sudoku

import java.util.Stack
import kotlin.math.sqrt

/**
 * Field of values, representing a sudoku.
 *
 * @param values The sudoku values.
 *
 * @throws IllegalArgumentException If the given field is not a square or not evenly distributed.
 * */
class Field(val values: Array<IntArray>) {

    internal val segDim = sqrt(values.size.toFloat()).toInt()

    init {
        require(values.all { it.size == values.size }) { "Square field required" }
        val innerSegDim = sqrt(values[0].size.toFloat()).toInt()
        require(segDim * segDim == values.size && innerSegDim == segDim) { "Evenly distributed segments required" }
    }

    /**
     * Collapse all possibilities inside the resulting Q map, e.g. solving the sudoku.
     *
     * @return Whether the sudoku was solved.
     * */
    @Suppress("unused")
    fun collapseAll(): Boolean {

        /* initial setup */
        val qPoints = Stack<QPoint>()  // backtrace stack
        var qPoint = QPoint(this) // current Q point

        val segDim = segDim
        val dim = values.size
        val nFields = dim * dim
        val cBit = 1 shl dim

        var lcBoundary = 1  // current lower collapse boundary [1-9]
        var pos = 0         // current field [0-80]

        /* solve */
        while(qPoint.cCount < nFields) {

            val y = pos / dim
            val x = pos++ % dim
            var mask = qPoint.qStatesNxN[y][x]

            // check highest bit to see,
            // whether the field state is collapsed
            if(mask and cBit != 0) {

                mask = mask and cBit.inv()
                val bits = mask.countOneBits() // ^= number of possibilities

                // check if below collapse boundary (lower = better)

                // force collapsing a state with the least amount of possibilities
                // results in the lowest error chance (backtracking speedup)
                if(bits <= lcBoundary) {
                    when(bits) {
                        0 -> {
                            // no possibilities left -> track backwards
                            if(qPoints.size == 0) return false // no solution
                            qPoint = qPoints.pop()
                        }
                        1 -> {
                            // collapse single possibility
                            qPoint.collapse(x, y, segDim, mask.takeLowestOneBit())
                        }
                        else -> {
                            // multiple possibilities -> backtracking fork
                            qPoints.push(qPoint)
                            // choose one state to collapse to
                            qPoint = qPoint.collapse1(x, y, segDim)
                        }
                    }
                    // success -> reset boundary
                    lcBoundary = 1
                    pos = 0
                }
            }

            // check if end is reached
            if(pos >= nFields) {
                // current boundary was too low to collapse anything -> increase value
                lcBoundary++
                pos = 0
            }
        }

        /* extract solution */
        // sudoku was solved -> convert Q map to chars
        for(y in 0 ..< dim) {
            for(x in 0 ..< dim) {
                values[y][x] = qPoint.qStatesNxN[y][x].countTrailingZeroBits() + 1
            }
        }

        return true
    }
}

/**
 * Single data point inside the Q mesh, representing a possible solution in the possibility tree.
 *
 * @param qStatesNxN The Q map to store all fields with their respective possibilities.
 * @param cCount The amount of collapsed fields inside the grid.
 * */
class QPoint private constructor(val qStatesNxN: Array<IntArray>, var cCount: Int) {

    /**
     * Create a new QPoint from a given QField, mapping all known values to a collapsed Q state.
     *
     * @param field The field to map.
     * */
    constructor(field: Field) : this(Array(field.values.size) { IntArray(field.values.size) }, 0) {

        val qAll = (1 shl field.values.size + 1) - 1
        // init Q map with all possibilities
        qStatesNxN.forEach { it.fill(qAll) }
        field.values.forEachIndexed { y, row ->
            row.forEachIndexed { x, value ->
                if(value != 0)
                    // pre-collapse known value
                    collapse(x, y, field.segDim, 1 shl (value - 1))
            }
        }
    }

    /**
     * Create a fork at the given position & collapse one possibility.
     *
     * @param x The current grid x coordinate.
     * @param y The current grid y coordinate.
     * @param segDim The dimension of the current segment.
     *
     * @return The newly forked QPoint.
     * */
    fun collapse1(x: Int, y: Int, segDim: Int): QPoint {

        // fork Q map
        val rPoint = Array(qStatesNxN.size) { qStatesNxN[it].copyOf() }

        // remove possibility from this Q map
        val qStates = qStatesNxN[y][x]
        val qState = qStates.takeLowestOneBit()
        qStatesNxN[y][x] = qStates and qState.inv()

        // return forked point
        return QPoint(rPoint, cCount).apply {
            // collapse possibility in forked point
            collapse(x, y, segDim, qState)
        }
    }

    /**
     * Collapse a given possibility at the given position.
     *
     * @param x The current grid x coordinate.
     * @param y The current grid y coordinate.
     * @param segDim The dimension of the current segment.
     * @param qState The possibility to collapse.
     * */
    fun collapse(x: Int, y: Int, segDim: Int, qState: Int) {

        // process segment (remove possibilities)
        val baseX = (x / segDim) * segDim
        val baseY = (y / segDim) * segDim
        for(posY in baseY ..< baseY + segDim) {
            for(posX in baseX ..< baseX + segDim) {
                qStatesNxN[posY][posX] = qStatesNxN[posY][posX] and qState.inv()
            }
        }

        // process row (remove possibilities)
        for(posX in qStatesNxN.indices) {
            qStatesNxN[y][posX] = qStatesNxN[y][posX] and qState.inv()
        }

        // process column (remove possibilities)
        for(posY in qStatesNxN.indices) {
            qStatesNxN[posY][x] = qStatesNxN[posY][x] and qState.inv()
        }

        // collapse field
        qStatesNxN[y][x] = qState
        cCount++
    }
}
