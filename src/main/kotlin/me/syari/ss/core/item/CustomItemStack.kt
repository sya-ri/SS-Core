package me.syari.ss.core.item

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.syari.ss.core.Main.Companion.corePlugin
import me.syari.ss.core.code.StringEditor.toColor
import me.syari.ss.core.persistentData.CustomPersistentData
import me.syari.ss.core.persistentData.CustomPersistentDataContainer
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

/**
 * [ItemStack] の拡張クラス
 */
class CustomItemStack constructor(private val item: ItemStack, amount: Int) : CustomPersistentDataContainer,
    ConfigurationSerializable {

    /**
     * アイテムの量
     */
    var amount = amount
        set(value) {
            item.amount = value
            field = value
        }

    /**
     * アイテムタイプ
     */
    var type: Material
        set(value) {
            item.type = value
        }
        get() = item.type

    /**
     * アイテム名が存在するか取得します
     */
    val hasDisplay get() = itemMeta?.hasDisplayName() ?: false

    /**
     * アイテム名
     */
    var display: String?
        set(value) {
            editMeta {
                setDisplayName(value?.toColor)
            }
        }
        get() = itemMeta?.displayName

    /**
     * アイテムの説明文が存在するか取得します
     */
    val hasLore get() = itemMeta?.hasLore() ?: false

    /**
     * アイテムの説明文
     */
    var lore: MutableList<String>
        set(value) {
            editMeta {
                lore = value.toColor
            }
        }
        get() = itemMeta?.lore ?: mutableListOf()

    /**
     * アイテムダメージ
     */
    var damage
        set(value) {
            editMeta {
                if (this is Damageable) {
                    damage = value
                }
            }
        }
        get() = (itemMeta as? Damageable)?.damage ?: 0

    /**
     * 耐久無限
     */
    var unbreakable: Boolean
        set(value) {
            editMeta {
                isUnbreakable = value
            }
        }
        get() = itemMeta?.isUnbreakable ?: false

    /**
     * アイテムメタが存在するか取得します
     */
    val hasItemMeta get() = item.hasItemMeta()

    /**
     * アイテムメタ
     */
    var itemMeta: ItemMeta?
        set(value) {
            item.itemMeta = value
        }
        get() = item.itemMeta

    /**
     * アイテムメタを変更した後、再代入されます
     * @param run アイテムメタに対して実行する処理
     */
    inline fun editMeta(run: ItemMeta.() -> Unit) {
        val meta = itemMeta ?: corePlugin.server.itemFactory.getItemMeta(type) ?: return
        run.invoke(meta)
        itemMeta = meta
    }

    /**
     * 対象アイテムフラグが存在するか取得します
     * @param flag 対象アイテムフラグ
     * @return [Boolean]
     */
    fun hasItemFlag(flag: ItemFlag): Boolean {
        return itemMeta?.hasItemFlag(flag) == true
    }

    /**
     * アイテムフラグを追加します
     * @param flag アイテムフラグ
     */
    fun addItemFlag(vararg flag: ItemFlag) {
        editMeta {
            addItemFlags(*flag)
        }
    }

    /**
     * アイテムフラグを削除します
     * @param flag アイテムフラグ
     */
    fun removeItemFlag(vararg flag: ItemFlag) {
        editMeta {
            removeItemFlags(*flag)
        }
    }

    /**
     * 対象エンチャントが存在するか取得します
     * @param enchant 対象エンチャント
     * @return [Boolean]
     */
    fun hasEnchant(enchant: Enchantment): Boolean {
        return itemMeta?.hasEnchant(enchant) == true
    }

    /**
     * エンチャントを追加します
     * @param enchant エンチャント
     * @param level レベル
     */
    fun addEnchant(enchant: Enchantment, level: Int) {
        editMeta {
            addEnchant(enchant, level, true)
        }
    }

    /**
     * エンチャントを削除します
     * @param enchant エンチャント
     */
    fun removeEnchant(enchant: Enchantment) {
        editMeta {
            removeEnchant(enchant)
        }
    }

    /**
     * [ItemStack] に変換します
     */
    val toItemStack: List<ItemStack>
        get() {
            val map = mutableListOf<ItemStack>()
            val stackNumber = amount / 64
            if (0 < stackNumber) {
                val stackItem = item.asQuantity(64)
                for (i in 0 until stackNumber) {
                    map.add(stackItem)
                }
            }
            val modNumber = amount % 64
            if (modNumber != 0) {
                val modItem = item.asQuantity(modNumber)
                modItem.amount = modNumber
                map.add(modItem)
            }
            return map
        }

    /**
     * [ItemStack] に変換します
     */
    val toOneItemStack: ItemStack
        get() {
            return item.clone().apply { amount = if (64 < amount) 64 else amount }
        }

    /**
     * 同じアイテムか判定します
     * @param item 対象アイテム
     * @return [Boolean]
     */
    fun isSimilar(item: CustomItemStack) = isSimilar(item.toOneItemStack)

    /**
     * 同じアイテムか判定します
     * @param item 対象アイテム
     * @return [Boolean]
     */
    fun isSimilar(item: ItemStack) = toOneItemStack.isSimilar(item)

    /**
     * PersistentData に編集を加えます
     * @see CustomPersistentData
     * @return [E]?
     */
    override fun <E> editPersistentData(plugin: JavaPlugin, run: CustomPersistentData.() -> E): E? {
        var result: E? = null
        editMeta {
            result = run.invoke(
                CustomPersistentData(
                    plugin,
                    persistentDataContainer
                )
            )
        }
        return result
    }

    /**
     * PersistentData を取得します
     * @return [CustomPersistentData]?
     */
    override fun getPersistentData(plugin: JavaPlugin): CustomPersistentData? {
        return itemMeta?.persistentDataContainer?.let { CustomPersistentData(plugin, it) }
    }

    /**
     * CustomItemStack を複製します
     * @return [CustomItemStack]
     */
    fun clone() = CustomItemStack(item.clone(), amount)

    /**
     * CustomItemStack を複製します
     * @param run 複製後のアイテムに対して実行する処理
     * @return [CustomItemStack]
     */
    fun clone(run: CustomItemStack.() -> Unit) = clone().apply { run.invoke(this) }

    /**
     * @see [ConfigurationSerializable]
     */
    override fun serialize(): MutableMap<String, Any> {
        return LinkedHashMap<String, Any>().also { result ->
            result["type"] = type.name
            if (amount != 1) {
                result["amount"] = amount
            }
            val meta = itemMeta
            if (meta != null && !corePlugin.server.itemFactory.equals(meta, null)) {
                result["meta"] = meta.serialize()
            }
        }
    }

    /**
     * Json に変換します
     * @return [String]
     */
    fun toJson(): String {
        return Gson().toJson(serialize())
    }

    /**
     * Base64 に変換します
     * @return [String]
     */
    fun toBase64(): String {
        return InventoryBase64.toBase64(toItemStack)
    }

    companion object {
        /**
         * @param item アイテム
         * @param amount アイテム数 default: item.amount
         * @return [CustomItemStack]
         */
        fun create(item: ItemStack?, amount: Int? = null): CustomItemStack {
            val data = if (item != null) {
                item to (amount ?: item.amount)
            } else {
                ItemStack(Material.AIR) to 0
            }
            return CustomItemStack(data.first, data.second)
        }

        /**
         * @param material アイテムタイプ
         * @param amount アイテム数 default: 1
         * @return [CustomItemStack]
         */
        fun create(material: Material?, amount: Int? = 1): CustomItemStack {
            return create(material?.let { ItemStack(it) }, amount)
        }

        /**
         * @param material アイテムタイプ
         * @param display アイテム名
         * @param lore アイテムの説明文
         * @param damage アイテムダメージ default: 0
         * @param amount アイテム数 default: 1
         * @return [CustomItemStack]
         */
        fun create(
            material: Material,
            display: String?,
            lore: List<String>,
            damage: Int = 0,
            amount: Int = 1
        ): CustomItemStack {
            return create(material, amount).apply {
                this.display = display
                this.lore = lore.toMutableList()
                this.damage = damage
            }
        }

        /**
         * @param material アイテムタイプ
         * @param display アイテム名
         * @param lore アイテムの説明文
         * @param damage アイテムダメージ default: 0
         * @param amount アイテム数 default: 1
         * @return [CustomItemStack]
         */
        fun create(
            material: Material,
            display: String?,
            vararg lore: String,
            damage: Int = 0,
            amount: Int = 1
        ): CustomItemStack {
            return create(material, display, lore.toList(), damage, amount)
        }

        /**
         * @param item アイテム
         * @param amount アイテム数 default: item.amount
         * @return [CustomItemStack]?
         */
        fun fromNullable(item: ItemStack?, amount: Int? = null): CustomItemStack? {
            return if (item != null) create(item, amount) else null
        }

        /**
         * Json をアイテムに変換します
         * @param json Json データ
         * @return [CustomItemStack]
         */
        fun fromJson(json: String): CustomItemStack {
            val map: Map<String, Any> = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
            return fromMap(map)
        }

        /**
         * Map<String, Any> をアイテムに変換します
         * @param args マップデータ
         * @return [CustomItemStack]
         */
        private fun fromMap(args: Map<String, Any>): CustomItemStack {
            val item = ItemStack(
                Material.getMaterial(args["type"] as String) ?: Material.STONE,
                1
            )

            val amount = if (args.containsKey("amount")) {
                (args["amount"] as Number).toInt()
            } else {
                1
            }

            return CustomItemStack(item, amount).apply {
                if (args.containsKey("meta")) {
                    @Suppress("UNCHECKED_CAST")
                    val itemMetaMap = args["meta"] as MutableMap<String, Any>
                    itemMetaMap["=="] = "ItemMeta"
                    itemMeta = ConfigurationSerialization.deserializeObject(itemMetaMap) as ItemMeta
                }
            }
        }

        /**
         * Base64 をアイテムに変換します
         * @param base64 Base64 データ
         * @return [CustomItemStack]
         */
        fun fromBase64(base64: String): List<CustomItemStack> {
            val items = InventoryBase64.getItemStackFromBase64(base64)
            return compress(items)
        }

        /**
         * 同じアイテムをまとめます
         * @param items アイテム
         * @return [List]<[CustomItemStack]>
         */
        fun compress(items: Iterable<ItemStack>): List<CustomItemStack> {
            return mutableListOf<CustomItemStack>().apply {
                items.forEach { item ->
                    val similarItem = firstOrNull { it.isSimilar(item) }
                    if (similarItem != null) {
                        similarItem.amount += item.amount
                    } else {
                        add(create(item))
                    }
                }
            }
        }
    }
}