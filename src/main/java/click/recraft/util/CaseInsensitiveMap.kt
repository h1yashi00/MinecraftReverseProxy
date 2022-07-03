package click.recraft.util

import gnu.trove.map.hash.TCustomHashMap

class CaseInsensitiveMap<V>: TCustomHashMap<String, V> {
    constructor(): super(CaseInsensitiveHashingStrategy.INSTANCE)
    constructor(map: Map<out String,out V>): super(CaseInsensitiveHashingStrategy.INSTANCE, map)
}