package sorting

import java.io.File
import java.util.Scanner
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val p = listOf("-sortingType", "-dataType", "-inputFile", "-outputFile")
    val a = args.filter { it.first() == '-' && it !in p }.toList()
    if (a.isNotEmpty()) a.forEach { println("\"$it\" is not a valid parameter. It will be skipped.") }

    val check = { arg: String, errorMessage: String, default: String? ->
        if ( arg in args) {
            if (arg.nextArgs(args) == null) {
                println(errorMessage)
                exitProcess(0)
            } else arg.nextArgs(args)!!
        } else default }

    val inFile = check("-inputFile", "No input File name defined!", null)
    val outFile = check("-outputFile", "No output File name defined!", null)
    val sorting = check("-sortingType", "No sorting type defined!", "natural")
    val dT = check("-dataType", "No data type defined!", null)

    val stringers = { dataType: String ->
        val str = Stringers(true, sorting!!, dataType, inFile, outFile)
        if (outFile != null) str.toOutputFile() else println(str)
    }

    val integer = {
        val int = Integers(true, sorting!!, inFile, outFile)
        if (outFile != null) int.toOutputFile() else println(int)
    }

    if (dT != null) {
        when (dT) {
            "long" -> integer()
            "line" -> stringers(dT)
            "word" -> stringers(dT)
        }
    } else stringers("word")

}

fun String.nextArgs(_args: Array<String>, _arg: String = toString()): String? {
    val nextIndex = _args.indexOf(_arg) + 1
    return if (nextIndex > _args.lastIndex) null else {
        val type = _args[_args.indexOf(_arg) + 1]
        if (type.first() == '-') null else type
    }
}






//--------------------------------------------------------------------------------------------------------------------
class Integers(_sort: Boolean, _typeSort: String, _inFile: String? = null, _outFile: String? = null) {
    val sort: Boolean = _sort


    val inFile = _inFile
    val outFile = _outFile


    val typeSort: String = _typeSort
    var inputList = mutableListOf<Int>()
        init {
            inToInputList()
        }
    val inputListCount = inputList.count()
    var inputListSort = mutableListOf<MutableList<Int>>()
    val inputListExtendedSort = mutableListOf<MutableList<Int>>()

    init {
        inputToListsSort( inputList, inputListSort)
        sorting(inputListSort, {i -> inputListSort[i][0]})
        inputToListsExtended(inputListSort, inputListExtendedSort)
        sorting(inputListExtendedSort, {i -> inputListExtendedSort[i][1]}, { i -> inputListExtendedSort[i][0] })
    }




    fun inToInputList() {
        val a = { str: String -> "\"$str\" is not a long. It will be skipped." }
        val b = { str: String -> try { str.toInt() } catch (e: NumberFormatException) { println(a(str)); null } }
        if (inFile != null) {
            val file = File(inFile)
            inputList += file.readText().split(" ").filter { it.trim().isNotEmpty() }.mapNotNull { b(it) }
        } else {
            val scanner = Scanner(System.`in`)
            while (scanner.hasNext()) {
                inputList += scanner.nextLine().split(" ").filter { it.trim().isNotEmpty() }.mapNotNull { b(it) }

            }
        }
    }

    fun toOutputFile() {
        if (outFile != null) {
            val file = File(outFile)
            file.writeText(this.toString())
        }
    }




    override fun toString(): String {
        return if (sort) {
            when (typeSort) {
                "byCount" -> {
                    var str = "Total numbers: $inputListCount.\n"
                    inputListExtendedSort.forEach {  str += "${it[0]}: ${it[1]} time(s), ${it[2]}%\n" }
                    str
                }
                else -> strNatural()
            }
        } else strNatural()
    }

    fun strNatural(): String {
        var str = ""
        inputListSort.forEach { str += "${it[0]} " }
        return "Total numbers: $inputListCount.\nSorted data: ${str}"
    }


    fun inputToListsSort(_in: MutableList<Int>, _out: MutableList<MutableList<Int>>) {
        for (i in 0 .. _in.lastIndex) {
                _out.add(i, mutableListOf())
                _out[i].add(0, _in[i])
        }
    }

    fun inputToListsExtended(_in: MutableList<MutableList<Int>>, _out: MutableList<MutableList<Int>>) {
        var ik = 0
        for (i in 0 .. _in.lastIndex) {
            if ((i - 1) >= 0 && _in[i-1][0] != _in[i][0] || i == 0) {
                _out.add(ik, mutableListOf())
                _out[ik].add(0, _in[i][0])
                _out[ik].add(1, _in.count { x -> x == _in[i] })
                val c = _in.count { x -> x == _in[i] }
                val p = (c * 100) / inputListCount
                val pp = (c * 100) % inputListCount
                _out[ik].add(2, if (pp < 5) p else p + 1)
                ik++
            }
        }
    }

    fun sorting(
        _unSortedList: MutableList<MutableList<Int>>,
        valueSort1: (i: Int) -> Int,
        valueSort2: (i: Int) -> Int = valueSort1
    )  {
        sortToRange(_unSortedList, valueSort1)
        /*
        val heapedUnSortedList = buildMaxHeap(_unSortedList , valueSort1)
        for (i in (heapedUnSortedList.lastIndex - 1) downTo 0) {
            extractMax(heapedUnSortedList, i, valueSort1)
        }*/
        if (valueSort1 != valueSort2) {
            // println("Begin sorting 2 to value")
            val k = _unSortedList.toMutableList().groupBy { it[1] }.mapValues { it.value.count() }.filterValues { it > 1 }.keys
            if (k.isNotEmpty()) {
                for (i in k) {
                    var imin = 0
                    var imax = 0
                    _unSortedList.forEachIndexed() { ind, it -> if (it[1] == i) if (ind > imax) imax = ind else if (ind < imin) imin = ind }
                    sortToRange(_unSortedList, valueSort2, imax, imin)
                }
            }
        }
    }

    fun sortToRange(
        _unSortedList: MutableList<MutableList<Int>>,
        valueSort1: (i: Int) -> Int,
        i_max: Int = _unSortedList.lastIndex,
        i_min: Int = 0
    ) {
        val heapedUnSortedList = buildMaxHeap(_unSortedList , valueSort1, i_max, i_min)
        // println(heapedUnSortedList)
        // println("Sort to range BEGIN")
        for (i in i_max - 1 downTo i_min) {
            extractMax(heapedUnSortedList, i, valueSort1, i_min)
            // println(heapedUnSortedList)
        }
        // println("Sort to range END")
    }

    fun buildMaxHeap(
        _unSortedList: MutableList<MutableList<Int>>,
        valueSort: (i: Int) -> Int,
        i_max: Int = _unSortedList.lastIndex,
        i_min: Int = 0
    ): MutableList<MutableList<Int>> {
        // println("Heaping BEGIN")
        // println(_unSortedList)
        for (i in i_max / 2 downTo i_min) {
            heapify(_unSortedList, i, i_max) { i_this: Int, i_another: Int -> compareValueSort(i_this, i_another, valueSort) }
            // println(_unSortedList)
        }
        // println("Heaping END")
        return _unSortedList
    }

    fun extractMax(
        unSortedList: MutableList<MutableList<Int>>,
        i_last: Int,
        valueSort: (i: Int) -> Int,
        i_min: Int = 0
    ) {
        unSortedList[i_min] = unSortedList[i_last + 1].also { unSortedList[i_last + 1] = unSortedList[i_min] }
        // println(unSortedList)
        heapify(unSortedList, i_min, i_last) { i_this: Int, i_another: Int -> compareValueSort(i_this, i_another, valueSort) }
    }

       fun compareValueSort(i_this: Int, i_another: Int, valueSort: (index: Int) -> Int): Int {
           return if (valueSort(i_this) != valueSort(i_another)) {
               valueSort(i_this) - valueSort(i_another)
           } else 0
       }

    fun heapify(
        _unSortedList: MutableList<MutableList<Int>>,
        _i_now: Int,
        i_last: Int,
        compareValueSort: (i_this: Int, i_another: Int) -> Int
    ): MutableList<MutableList<Int>> {
        var i_now = _i_now
        while (i_now < i_last && i_now < _unSortedList.lastIndex) {
            val i_left = i_now * 2 + 1
            val i_right = i_now * 2 + 2
            var i_largest = i_now
            if (i_left <= i_last && i_left <= _unSortedList.lastIndex && compareValueSort(i_left, i_largest) > 0) {
                i_largest = i_left
            }
            if (i_right <= i_last && i_right <= _unSortedList.lastIndex && compareValueSort(i_right, i_largest) > 0) {
                i_largest = i_right
            }
            if (i_largest == i_now) {
                break
            } else {
                _unSortedList[i_now] = _unSortedList[i_largest].also { _unSortedList[i_largest] = _unSortedList[i_now] }
                i_now = i_largest
            }
        }
        return _unSortedList
    }
}


//--------------------------------------------------------------------------------------------------------------------


class Stringers(_sort: Boolean, _typeSort: String, _dataType: String, _inFile: String? = null, _outFile: String? = null) {
    val sort: Boolean = _sort

    val inFile = _inFile
    val outFile = _outFile

    val dataType: String = _dataType
    val typeSort: String = _typeSort
    val inputList = mutableListOf<String>()
    init {
        inToInputList()
    }
    val inputListCount = inputList.count()
    var inputListSort = mutableListOf<MutableList<String>>()
    val inputListExtendedSort = mutableListOf<MutableList<String>>()

    init {
        inputToListsSort( inputList, inputListSort)
        sorting(inputListSort, { i -> inputListSort[i][0]})
        inputToListsExtended(inputListSort, inputListExtendedSort)
        sorting(inputListExtendedSort, { i -> inputListExtendedSort[i][1]}, { i -> inputListExtendedSort[i][0]})
    }

    fun inToInputList() {
        if (inFile != null) {
            val file = File(inFile)
            if (dataType == "line") inputList += file.readLines().filter { it.trim().isNotEmpty() }
            else inputList += file.readText().split(" ").filter { it.trim().isNotEmpty() }
        } else {
            val scanner = Scanner(System.`in`)
            while (scanner.hasNext()) {
                if (dataType == "line") inputList += scanner.nextLine()
                else inputList += scanner.nextLine().split(" ").filter { it.trim().isNotEmpty() }
            }
        }
    }

    fun toOutputFile() {
        if (outFile != null) {
            val file = File(outFile)
            file.writeText(this.toString())
        }
    }

    override fun toString(): String {
        return if (sort) {
            when (typeSort) {
                "byCount" -> {
                    var str = "Total numbers: $inputListCount.\n"
                    inputListExtendedSort.forEach {  str += "${it[0]}: ${it[1]} time(s), ${it[2]}%\n" }
                    str
                }
                else -> strNatural()
            }
        } else strNatural()
    }

    fun strNatural(): String {
        var str = ""
        if (dataType == "line") {
            inputListSort.forEach { str += "${it[0]}\n" }
            return "Total numbers: $inputListCount.\nSorted data:\n${str}"
        } else {
            inputListSort.forEach { str += "${it[0]} " }
            return "Total numbers: $inputListCount.\nSorted data: ${str}"
        }
    }

    fun inputToListsSort(_in: MutableList<String>, _out: MutableList<MutableList<String>>) {
        for (i in 0 .. _in.lastIndex) {
            _out.add(i, mutableListOf())
            _out[i].add(0, _in[i])
        }
    }

    fun inputToListsExtended(_in: MutableList<MutableList<String>>, _out: MutableList<MutableList<String>>) {
        var ik = 0
        for (i in 0 .. _in.lastIndex) {
            if ((i - 1) >= 0 && _in[i-1][0] != _in[i][0] || i == 0) {
                _out.add(ik, mutableListOf())
                _out[ik].add(0, _in[i][0])
                _out[ik].add(1, _in.count { x -> x == _in[i] }.toString())
                val c = _in.count { x -> x == _in[i] }
                val p = (c * 100) / inputListCount
                val pp = (c * 100) % inputListCount
                _out[ik].add(2, (if (pp < 5) p else p + 1).toString())
                ik++
            }
        }
    }

    fun sorting(
        _unSortedList: MutableList<MutableList<String>>,
        valueSort1: (i: Int) -> String,
        valueSort2: (i: Int) -> String = valueSort1
    )  {
        sortToRange(_unSortedList, valueSort1)
        /*
        val heapedUnSortedList = buildMaxHeap(_unSortedList , valueSort1)
        for (i in (heapedUnSortedList.lastIndex - 1) downTo 0) {
            extractMax(heapedUnSortedList, i, valueSort1)
        }*/
        if (valueSort1 != valueSort2) {
            // println("Begin sorting 2 to value")
            val k = _unSortedList.toMutableList().groupBy { it[1] }.mapValues { it.value.count() }.filterValues { it > 1 }.keys
            if (k.isNotEmpty()) {
                for (i in k) {
                    var imin = 0
                    var imax = 0
                    _unSortedList.forEachIndexed() { ind, it -> if (it[1] == i) if (ind > imax) imax = ind else if (ind < imin) imin = ind }
                    sortToRange(_unSortedList, valueSort2, imax, imin)
                }
            }
        }
    }


    fun sortToRange(
        _unSortedList: MutableList<MutableList<String>>,
        valueSort1: (i: Int) -> String,
        i_max: Int = _unSortedList.lastIndex,
        i_min: Int = 0
    ) {
        val heapedUnSortedList = buildMaxHeap(_unSortedList , valueSort1, i_max, i_min)
        // println(heapedUnSortedList)
        // println("Sort to range BEGIN")
        for (i in i_max - 1 downTo i_min) {
            extractMax(heapedUnSortedList, i, valueSort1, i_min)
        // println(heapedUnSortedList)
        }
        // println("Sort to range END")
    }

    fun buildMaxHeap(
        _unSortedList: MutableList<MutableList<String>>,
        valueSort: (i: Int) -> String,
        i_max: Int = _unSortedList.lastIndex,
        i_min: Int = 0
    ): MutableList<MutableList<String>> {
        // println("Heaping BEGIN")
        // println(_unSortedList)
        for (i in i_max / 2 downTo i_min) {
            heapify(_unSortedList, i, i_max) { i_this: Int, i_another: Int -> compareValueSort(i_this, i_another, valueSort) }
            // println(_unSortedList)
        }
        // println("Heaping END")
        return _unSortedList
    }

    fun extractMax(
        unSortedList: MutableList<MutableList<String>>,
        i_last: Int,
        valueSort: (i: Int) -> String,
        i_min: Int = 0
    ) {
        unSortedList[i_min] = unSortedList[i_last + 1].also { unSortedList[i_last + 1] = unSortedList[i_min] }
        // println(unSortedList)
        heapify(unSortedList, i_min, i_last) { i_this: Int, i_another: Int -> compareValueSort(i_this, i_another, valueSort) }
    }

    fun compareValueSort(i_this: Int, i_another: Int, valueSort: (index: Int) -> String): Int {
         if (valueSort(i_this) != valueSort(i_another)) {
             val minimal: String
             //val maxL: Int
             val minL = if (valueSort(i_this).length > valueSort(i_another).length) {
                 minimal = valueSort(i_another)
                 //maxL = valueSort(i_this).length
                 valueSort(i_another).length
             } else {
                 minimal = valueSort(i_this)
                 //maxL = valueSort(i_another).length
                 valueSort(i_this).length
             }
             for (i in 0 .. minL - 1) {
                 if (valueSort(i_this)[i] != valueSort(i_another)[i]) {
                     return valueSort(i_this)[i] - valueSort(i_another)[i]
             }
             }
             return if (minimal == valueSort(i_another)) 1 else -1
             /*minimal.padEnd(maxL)
                 for (i in minL + 1 .. maxL) {
                     if (valueSort(i_this)[i] != valueSort(i_another)[i]) {
                         minimal.trimEnd()
                         return valueSort(i_this)[i] - valueSort(i_another)[i]
                     }    */
        } else return 0
    }

    fun heapify(
        _unSortedList: MutableList<MutableList<String>>,
        _i_now: Int,
        i_last: Int,
        compareValueSort: (i_this: Int, i_another: Int) -> Int
    ): MutableList<MutableList<String>> {
        var i_now = _i_now
        while (i_now < i_last && i_now < _unSortedList.lastIndex) {
            val i_left = i_now * 2 + 1
            val i_right = i_now * 2 + 2
            var i_largest = i_now
            if (i_left <= i_last && i_left <= _unSortedList.lastIndex && compareValueSort(i_left, i_largest) > 0) {
                i_largest = i_left
            }
            if (i_right <= i_last && i_right <= _unSortedList.lastIndex && compareValueSort(i_right, i_largest) > 0) {
                i_largest = i_right
            }
            if (i_largest == i_now) {
                break
            } else {
                _unSortedList[i_now] = _unSortedList[i_largest].also { _unSortedList[i_largest] = _unSortedList[i_now] }
                i_now = i_largest
            }
        }
        return _unSortedList
    }
}