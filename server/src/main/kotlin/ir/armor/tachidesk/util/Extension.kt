package ir.armor.tachidesk.util

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import com.googlecode.dex2jar.tools.Dex2jarCmd
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.SourceFactory
import eu.kanade.tachiyomi.source.online.HttpSource
import ir.armor.tachidesk.APKExtractor
import ir.armor.tachidesk.database.table.ExtensionTable
import ir.armor.tachidesk.database.table.SourceTable
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okio.buffer
import okio.sink
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.net.URL
import java.net.URLClassLoader

fun installAPK(apkName: String, apksDir: String): Int {
//    val extensionRecord = getExtensionList(true).first { it.apkName == apkName }
    val fileNameWithoutType = apkName.substringBefore(".apk")
    val dirPathWithoutType = "$apksDir/$fileNameWithoutType"

    // check if we don't have the dex file already downloaded
    val jarPath = "$apksDir/$fileNameWithoutType.jar"
    if (!File(jarPath).exists()) {
        runBlocking {
            val apkFilePath = "$dirPathWithoutType.apk"
            val jarFilePath = "$dirPathWithoutType.jar"
            val dexFilePath = "$dirPathWithoutType.dex"

            val className: String = APKExtractor.extract_dex_and_read_className(apkFilePath, dexFilePath)
            println(className)
            // dex -> jar
            Dex2jarCmd.main(dexFilePath, "-o", jarFilePath, "--force")

            // clean up
//            File(apkFilePath).delete()
            File(dexFilePath).delete()

            // update sources of the extension
            val child = URLClassLoader(arrayOf<URL>(URL("file:$jarFilePath")), this::class.java.classLoader)
            val classToLoad = Class.forName(className, true, child)
            val instance = classToLoad.newInstance()

            val extensionId = transaction {
                ExtensionTable.insertAndGetId {
                    it[ExtensionTable.apkName] = apkName
                }
            }

            if (instance is HttpSource) { // single source
                val httpSource = instance
                transaction {
//                            SourceEntity.new  {
//                                sourceId = httpSource.id
//                                name = httpSource.name
//                                this.extension =  ExtensionEntity.find { ExtensionsTable.name eq extension.name }.first().id
//                            }
                    if (SourceTable.select { SourceTable.id eq httpSource.id }.count() == 0L) {
                        SourceTable.insert {
                            it[this.id] = httpSource.id
                            it[name] = httpSource.name
                            it[this.lang] = httpSource.lang
                            it[extension] = extensionId
                        }
                    }
//                            println(httpSource.id)
//                            println(httpSource.name)
//                            println()
                }
            } else { // multi source
                val sourceFactory = instance as SourceFactory
                transaction {
                    sourceFactory.createSources().forEachIndexed { index, source ->
                        val httpSource = source as HttpSource
                        if (SourceTable.select { SourceTable.id eq httpSource.id }.count() == 0L) {
                            SourceTable.insert {
                                it[this.id] = httpSource.id
                                it[name] = httpSource.name
                                it[this.lang] = httpSource.lang
                                it[extension] = extensionId
                                it[partOfFactorySource] = true
                                it[positionInFactorySource] = index
                            }
                        }
//                                println(httpSource.id)
//                                println(httpSource.name)
//                                println()
                    }
                }
            }

            // update extension info
            transaction {
                ExtensionTable.update({ ExtensionTable.apkName eq apkName }) {
                    it[classFQName] = className
                }
            }
        }
        return 201 // we downloaded successfully
    } else {
        return 302
    }
}

val networkHelper: NetworkHelper by injectLazy()

private fun downloadAPKFile(url: String, apkPath: String) {
    val request = Request.Builder().url(url).build()
    val response = networkHelper.client.newCall(request).execute()

    val downloadedFile = File(apkPath)
    val sink = downloadedFile.sink().buffer()
    sink.writeAll(response.body!!.source())
    sink.close()
}

// fun removeExtension(pkgName: String) {
//    val extensionRecord = getExtensionList(true).first { it.apkName == pkgName }
//    val fileNameWithoutType = pkgName.substringBefore(".apk")
//    val jarPath = "${Config.extensionsRoot}/$fileNameWithoutType.jar"
//    transaction {
//        val extensionId = ExtensionTable.select { ExtensionTable.name eq extensionRecord.name }.first()[ExtensionTable.id]
//
//        SourceTable.deleteWhere { SourceTable.extension eq extensionId }
//        ExtensionTable.update({ ExtensionTable.name eq extensionRecord.name }) {
//            it[ExtensionTable.installed] = false
//        }
//    }
//
//    if (File(jarPath).exists()) {
//        File(jarPath).delete()
//    }
// }

// val network: NetworkHelper by injectLazy()

// fun getExtensionIcon(apkName: String): Pair<InputStream, String> {
//    val iconUrl = transaction { ExtensionTable.select { ExtensionTable.apkName eq apkName }.firstOrNull()!! }[ExtensionTable.iconUrl]
//
//    val saveDir = "${Config.extensionsRoot}/icon"
//    val fileName = apkName
//
//    return getCachedResponse(saveDir, fileName) {
//        network.client.newCall(
//            GET(iconUrl)
//        ).execute()
//    }
// }

// fun getExtensionIconUrl(apkName: String): String {
//    return "http://127.0.0.1:4567/api/v1/extension/icon/$apkName"
// }
