# sudoku

Simple sudoku solver, using backtracking & q-state collapse.

## Example Usage

```Kotlin
import cheatahh.sudoku.Field

val sudoku9x9 = Field(arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 0, 3, 0, 8, 5),
    intArrayOf(0, 0, 1, 0, 2, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 5, 0, 7, 0, 0, 0),
    intArrayOf(0, 0, 4, 0, 0, 0, 1, 0, 0),
    intArrayOf(0, 9, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(5, 0, 0, 0, 0, 0, 0, 7, 3),
    intArrayOf(0, 0, 2, 0, 1, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 4, 0, 0, 0, 9)
))

println("Success: ${sudoku9x9.collapseAll()}")
sudoku9x9.values.forEach { println(it.contentToString()) }
```

```Kotlin
import cheatahh.sudoku.Field

val sudoku4x4 = Field(arrayOf(
    intArrayOf(1, 2, 0, 0),
    intArrayOf(0, 3, 0, 0),
    intArrayOf(0, 0, 1, 0),
    intArrayOf(3, 0, 0, 0)
))

println("Success: ${sudoku4x4.collapseAll()}")
sudoku4x4.values.forEach { println(it.contentToString()) }
```