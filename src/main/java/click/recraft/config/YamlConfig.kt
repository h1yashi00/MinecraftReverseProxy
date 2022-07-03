package click.recraft.config

import click.recraft.server.MinecraftProxy
import click.recraft.util.CaseInsensitiveMap
import io.netty.util.CharsetUtil
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.*
import java.lang.RuntimeException

class YamlConfig {
    private val file = File("config.yaml")
    private val yaml: Yaml

    init {
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        yaml = Yaml(options)
    }
    private var config: MutableMap<String, Any>? = null

    fun <T: Any> get(path: String, def: T): T {
        return get(path, def, config!!)
    }

    private fun <T : Any> get(path: String, def: T, submap: MutableMap<String, Any>): T {
        var value = submap[path]
        if (value == null && def != null) {
            value = def
            submap[path] = def
            save()
        }
        return value as T
    }
    private fun set(path: String, value: Any) {
        set(path, value, config!!)
    }

    private fun set(path: String, value: Any, submap: MutableMap<String, Any>) {
        submap[path] = value
        save()
    }

    private fun save() {
        try {
            val wr = OutputStreamWriter(FileOutputStream(file), CharsetUtil.UTF_8)
            yaml.dump(config, wr)
        } catch (ex: IOException) {
            MinecraftProxy.logger.warning("could not save config ${ex.message}")
        }
    }

    fun load() {
        try {
            file.createNewFile()
            try {
                val inputStream = FileInputStream(file)
                config = yaml.load(inputStream) as MutableMap<String, Any>?
            } catch (e: YAMLException) {
                throw RuntimeException("Invalid configuration was encountered")
            }
            config = if (config == null) {
                CaseInsensitiveMap()
            } else {
                CaseInsensitiveMap(config!!)
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not load configuration !", e)
        }
    }
}