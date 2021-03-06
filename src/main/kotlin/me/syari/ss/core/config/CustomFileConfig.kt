package me.syari.ss.core.config

import me.syari.ss.core.Main.Companion.coreLogger
import me.syari.ss.core.message.Message.send
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

/**
 * @param plugin コンフィグがあるプラグイン
 * @param output メッセージの出力先
 * @param fileName ファイル名
 * @param directory ファイルの親フォルダ
 * @param deleteIfEmpty 中身が存在しなければ消去する
 */
class CustomFileConfig internal constructor(
    override val plugin: JavaPlugin,
    private val output: CommandSender,
    val fileName: String,
    private val directory: File,
    private val deleteIfEmpty: Boolean,
    default: Map<String, Any> = emptyMap()
): CustomConfig {
    private var file = File(directory, fileName)
    override val config: YamlConfiguration
    private val filePath: String

    init {
        filePath = file.path.substringAfter(plugin.dataFolder.path).substring(1)

        val writeDefault = if (!file.exists()) {
            try {
                file.createNewFile()
                coreLogger.info("$filePath の作成に成功しました")
                true
            } catch (ex: IOException) {
                coreLogger.error("$filePath の作成に失敗しました")
                false
            }
        } else if (file.length() == 0L && deleteIfEmpty) {
            coreLogger.warn("$filePath は中身が存在しないので削除されます")
            delete()
            false
        } else {
            false
        }
        config = YamlConfiguration.loadConfiguration(file)
        if (writeDefault && default.isNotEmpty()) {
            default.forEach { (key, value) ->
                set(key, value)
            }
            save()
        }
    }

    /**
     * 拡張子を除いたファイル名
     */
    val fileNameWithoutExtension = file.nameWithoutExtension

    /**
     * @param path コンフィグパス
     * @param value 上書きする値
     * @param save 上書き後に保存する default: false
     */
    fun set(
        path: String,
        value: Any?,
        save: Boolean = false
    ) {
        config.set(path, value)
        if (save) save()
    }

    /**
     * ファイルの名前を変更します
     * @param newName 新しい名前
     */
    fun rename(newName: String): Boolean {
        if (file.list()?.contains(newName) != false) return false
        return try {
            file.renameTo(File(directory, newName))
            true
        } catch (ex: SecurityException) {
            false
        } catch (ex: NullPointerException) {
            false
        }
    }

    /**
     * ファイルの変更を保存します
     */
    fun save() {
        config.save(file)
        if (deleteIfEmpty && file.length() == 0L) {
            delete()
        }
    }

    /**
     * ファイルを削除します
     */
    fun delete() {
        file.delete()
        coreLogger.info("$filePath の削除に成功しました")
    }

    /**
     * エラーを出力します
     * ```
     * Format: "&6[$filePath|$path] &c$message"
     * ```
     * @param path コンフィグパス
     * @param message 本文
     */
    override fun sendError(
        path: String,
        message: String
    ) {
        output.send("&6[$filePath|$path] &c$message")
    }
}