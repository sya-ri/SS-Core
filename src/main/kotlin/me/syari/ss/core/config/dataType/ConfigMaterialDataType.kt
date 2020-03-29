package me.syari.ss.core.config.dataType

import me.syari.ss.core.config.CustomConfig
import org.bukkit.Material

object ConfigMaterialDataType : ConfigDataType<Material> {
    override val typeName = "String(Material)"

    override fun get(config: CustomConfig, path: String, notFoundError: Boolean): Material? {
        val getValue = config.get(path, CustomConfig.STRING, notFoundError) ?: return null
        return Material.getMaterial(getValue)
    }
}