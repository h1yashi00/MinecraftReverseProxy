package click.recraft.util

import gnu.trove.strategy.HashingStrategy
import java.util.*


class CaseInsensitiveHashingStrategy : HashingStrategy<Any> {
    companion object {
        val INSTANCE = CaseInsensitiveHashingStrategy()
    }

    override fun computeHashCode(`object`: Any): Int {
        return (`object` as String).toLowerCase(Locale.ROOT).hashCode()
    }

    override fun equals(o1: Any, o2: Any): Boolean {
        return o1 == o2 || o1 is String && o2 is String && (o1 as String).equals((o2 as String), ignoreCase = true)
    }
}