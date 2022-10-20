import java.util.PriorityQueue
import kotlin.collections.ArrayDeque
import kotlin.io.path.Path
import kotlin.io.path.readLines


typealias CityLength = Pair<Int, Int>
typealias CityAnswer = Pair<Int, Answer>

// ДД = 26
// ММ = 03
// (26 + 3) mod 10 + 1 = 10
// Харьков - Нижний Новгород

fun main(args: Array<String>) {

    val from = "Харьков"
    val to = "Нижний Новгород"

    val citiesHeap = mutableListOf<String>()
    val citiesIdx = mutableMapOf<String, Int>()
    val citiesWays = Array(39) { ArrayDeque<CityLength>() }

    Path("data.txt").readLines().forEach {
        val (city1, city2, length) = it.split(",")
        if (!citiesIdx.containsKey(city1)) {
            citiesHeap.add(city1)
            citiesIdx[city1] = citiesHeap.size - 1
        }
        if (!citiesIdx.containsKey(city2)) {
            citiesHeap.add(city2)
            citiesIdx[city2] = citiesHeap.size - 1
        }
        val city1_idx = citiesIdx[city1]!!
        val city2_idx = citiesIdx[city2]!!
        citiesWays[city1_idx].add(city2_idx to length.toInt())
        citiesWays[city2_idx].add(city1_idx to length.toInt())
    }

    val fromIdx = citiesIdx[from]!!
    val toIdx = citiesIdx[to]!!

    if (args.isEmpty()) {
        translateAndPrint(bfs(citiesIdx, citiesWays, fromIdx, toIdx), citiesHeap)
        println("______________")
        println("Поиск в глубину")
        translateAndPrint(dfs(citiesIdx, citiesWays, fromIdx, toIdx, mutableSetOf(fromIdx), Answer(0, listOf(fromIdx))), citiesHeap)
        println("______________")
        translateAndPrint(dls(citiesIdx, citiesWays, fromIdx, toIdx, limit = 5), citiesHeap)
        println("______________")
        translateAndPrint(iddfs(citiesIdx, citiesWays, fromIdx, toIdx), citiesHeap)
        println("______________")
        translateAndPrint(bidirectionalSearch(citiesIdx, citiesWays, fromIdx, toIdx), citiesHeap)
        println("______________")
        translateAndPrint(bestFirstSearch(citiesWays, fromIdx, toIdx), citiesHeap)
        println("______________")
        translateAndPrint(aStar(citiesIdx, citiesWays, fromIdx, toIdx), citiesHeap)
    } else {
        for (arg in args) {
            val res = when (arg) {
                "dfs" -> {
                    println("Поиск в глубину")
                    dfs(citiesIdx, citiesWays, fromIdx, toIdx)
                }
                "bfs" -> bfs(citiesIdx, citiesWays, fromIdx, toIdx)
                else -> Answer()
            }
            println(res)
        }
    }
}

data class Answer(var length: Int = 0, var way: List<Int> = listOf())

fun bfs(
    citiesIdx: MutableMap<String, Int>,
    citiesWays: Array<ArrayDeque<CityLength>>,
    fromIdx: Int,
    toIdx: Int
): Answer {
    println("Поиск в ширину")
    return bfsCall(citiesIdx, citiesWays, fromIdx, toIdx)
}

fun bfsCall(
    citiesIdx: MutableMap<String, Int>,
    citiesWays: Array<ArrayDeque<CityLength>>,
    fromIdx: Int,
    toIdx: Int
): Answer {
    val visited = mutableSetOf<Int>()

    val queue = ArrayDeque<CityAnswer>()
    queue.add(fromIdx to Answer(0, listOf(fromIdx)))

    while (queue.isNotEmpty()) {
        val curr = queue.removeFirst()
        if (curr.first == toIdx) {
            return curr.second
        }
        queue.addFirst(curr)
        iterateThroughCityWaysAndAddToQueue(queue, citiesWays, visited)
    }

    return Answer()
}

fun dfs(citiesIdx: MutableMap<String, Int>, citiesWays: Array<ArrayDeque<CityLength>>, fromIdx: Int,
        toIdx: Int, visited: MutableSet<Int> = mutableSetOf(), curr: Answer = Answer()): Answer {
    if (fromIdx == toIdx) return curr

    for (city in citiesWays[fromIdx]) {
        if (!visited.contains(city.first)) {
            visited.add(city.first)
            val res = dfs(citiesIdx, citiesWays, city.first, toIdx, visited, Answer(city.second+curr.length,
                listOf(*curr.way.toTypedArray(), city.first)))
            if ( res != Answer() ) return res
            visited.remove(city.first)
        }
    }

    return Answer()
}

fun dfsBest(citiesIdx: MutableMap<String, Int>, citiesWays: Array<ArrayDeque<CityLength>>, fromIdx: Int,
        toIdx: Int, visited: MutableSet<Int> = mutableSetOf(), curr: Answer = Answer(), best: Answer = Answer(length = Int.MAX_VALUE)): Answer {
    if (fromIdx == toIdx) return curr

    for (city in citiesWays[fromIdx]) {
        if (!visited.contains(city.first)) {
            visited.add(city.first)
            val res = dfsBest(citiesIdx, citiesWays, city.first, toIdx, visited, Answer(city.second+curr.length,
                listOf(*curr.way.toTypedArray(), city.first)), best)
            if ( res != Answer() ) {
                if (best.length > res.length) {
                    best.length = res.length
                    best.way = res.way
                }
            }
            visited.remove(city.first)
        }
    }

    return best
}


fun dls(citiesIdx: MutableMap<String, Int>, citiesWays: Array<ArrayDeque<CityLength>>, fromIdx: Int,
        toIdx: Int, limit: Int): Answer {
    println("Поиск с ограничением глубины")
    return dlsRecursive(citiesIdx, citiesWays, fromIdx, toIdx, mutableSetOf(fromIdx),
        Answer(way = listOf(fromIdx)), limit, 0)
}

fun dlsRecursive(citiesIdx: MutableMap<String, Int>, citiesWays: Array<ArrayDeque<CityLength>>, fromIdx: Int,
                 toIdx: Int, visited: MutableSet<Int> = mutableSetOf(), curr: Answer = Answer(), limit: Int, currLevel: Int): Answer {
    if (fromIdx == toIdx) return curr
    if (limit == currLevel) return Answer()

    for (city in citiesWays[fromIdx]) {
        if (!visited.contains(city.first)) {
            visited.add(city.first)
            val res = dlsRecursive(citiesIdx, citiesWays, city.first, toIdx, visited, Answer(city.second+curr.length,
                listOf(*curr.way.toTypedArray(), city.first)), limit, currLevel+1)
            if ( res != Answer() ) return res
            visited.remove(city.first)
        }
    }

    return Answer()
}

fun iddfs(citiesIdx: MutableMap<String, Int>, citiesWays: Array<ArrayDeque<CityLength>>, fromIdx: Int,
          toIdx: Int): Answer {
    println("Поиск с итеративным углублением")
    var prev = Answer()
    var currLimit = 1
    while (prev == Answer()) {
        val curr = dlsRecursive(citiesIdx, citiesWays, fromIdx, toIdx, mutableSetOf(fromIdx),
            Answer(way = listOf(fromIdx)), currLimit, 0)
        prev = curr
        currLimit++
    }

    return prev
}

fun bidirectionalSearch(
    citiesIdx: MutableMap<String, Int>,
    citiesWays: Array<ArrayDeque<CityLength>>,
    fromIdx: Int,
    toIdx: Int
): Answer {
    println("Двунаправленный поиск")
    val visitedForward = mutableSetOf<Int>()
    val visitedBackward = mutableSetOf<Int>()

    val queueForward = ArrayDeque<CityAnswer>()
    queueForward.add(fromIdx to Answer(0, listOf(fromIdx)))
    val queueBackward = ArrayDeque<CityAnswer>()
    queueBackward.add(toIdx to Answer(0, listOf(toIdx)))

    while (queueForward.isNotEmpty() && queueBackward.isNotEmpty()) {
        iterateThroughCityWaysAndAddToQueue(queueForward, citiesWays, visitedForward)

        iterateThroughCityWaysAndAddToQueue(queueBackward, citiesWays, visitedBackward)

        // If intersecting vertex is found
        // that means there exist a path
        val intersections = visitedBackward.intersect(visitedForward).filterNot { it == fromIdx || it == toIdx }
        if (intersections.isNotEmpty()) {
            for (vertex in intersections) {
                println("Общий узел - $vertex")
                val forward = bfsCall(citiesIdx, citiesWays, fromIdx, vertex)
                val backward = bfsCall(citiesIdx, citiesWays, toIdx, vertex)
                return Answer(
                    length = forward.length + backward.length,
                    way = forward.way + backward.way.dropLast(1).reversed()
                )
            }
        }
    }

    return Answer()
}

private fun iterateThroughCityWaysAndAddToQueue(
    queueForward: ArrayDeque<CityAnswer>,
    citiesWays: Array<ArrayDeque<CityLength>>,
    visitedBackward: MutableSet<Int>
) {
    val currBackward = queueForward.removeFirst()
    for (cityLength in citiesWays[currBackward.first]) {
        if (!visitedBackward.contains(cityLength.first)) {
            visitedBackward.add(cityLength.first)
            queueForward.add(
                cityLength.first to Answer(
                    cityLength.second + currBackward.second.length,
                    listOf(*currBackward.second.way.toTypedArray(), cityLength.first)
                )
            )
        }
    }
}

fun bestFirstSearch(
    citiesWays: Array<ArrayDeque<CityLength>>,
    fromIdx: Int,
    toIdx: Int
): Answer {
    println("Поиск по первому наилучшему совпадению")
    val visited = mutableSetOf<Int>()

    val queue = PriorityQueue<CityAnswer> { cityAns1, cityAns2 -> cityAns1.second.length - cityAns2.second.length}
    return informationSearch(queue, fromIdx, toIdx, citiesWays, visited)
}

fun aStar(
    citiesIdx: MutableMap<String, Int>,
    citiesWays: Array<ArrayDeque<CityLength>>,
    fromIdx: Int,
    toIdx: Int
): Answer {
    println("A* Поиск")

    // нахождение h(v) - равно весу кратчайшего пути от v до цели.
    val h = Array(citiesWays.size) { 0 }
    citiesIdx.map { it.value }.filterNot { it == fromIdx || it == toIdx }.forEach {
        h[it] = bfsCall(citiesIdx, citiesWays, it, toIdx).length
    }

    val visited = mutableSetOf<Int>()

    val queue = PriorityQueue<CityAnswer> { cityAns1, cityAns2 ->
        (cityAns1.second.length + h[cityAns1.first]) - (cityAns2.second.length + h[cityAns2.first])}
    return informationSearch(queue, fromIdx, toIdx, citiesWays, visited)
}

private fun informationSearch(
    queue: PriorityQueue<CityAnswer>,
    fromIdx: Int,
    toIdx: Int,
    citiesWays: Array<ArrayDeque<CityLength>>,
    visited: MutableSet<Int>
): Answer {
    queue.add(fromIdx to Answer(0, listOf(fromIdx)))

    while (queue.isNotEmpty()) {
        val curr = queue.poll()
        if (curr.first == toIdx) {
            return curr.second
        }

        for (cityLength in citiesWays[curr.first]) {
            if (!visited.contains(cityLength.first)) {
                visited.add(cityLength.first)
                queue.add(
                    cityLength.first to Answer(
                        length = cityLength.second + curr.second.length,
                        way = listOf(*curr.second.way.toTypedArray(), cityLength.first)
                    )
                )
            }
        }
    }

    return Answer()
}

fun translateAndPrint(answer: Answer, citiesHeap: List<String>) {
    println(answer)
    println(answer.way.map { citiesHeap[it] })
}