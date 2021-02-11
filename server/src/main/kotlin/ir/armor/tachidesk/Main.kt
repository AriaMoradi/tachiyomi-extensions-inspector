package ir.armor.tachidesk

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.App
import ir.armor.tachidesk.util.applicationSetup
import ir.armor.tachidesk.util.getSourceList
import ir.armor.tachidesk.util.installAPK
import org.kodein.di.DI
import org.kodein.di.conf.global
import xyz.nulldev.androidcompat.AndroidCompat
import xyz.nulldev.androidcompat.AndroidCompatInitializer
import xyz.nulldev.ts.config.ConfigKodeinModule
import xyz.nulldev.ts.config.GlobalConfigManager
import java.io.File
import java.lang.RuntimeException

class Main {
    companion object {
        val androidCompat by lazy { AndroidCompat() }

        fun registerConfigModules() {
            GlobalConfigManager.registerModules(
//                    ServerConfig.register(GlobalConfigManager.config),
//                    SyncConfigModule.register(GlobalConfigManager.config)
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            // make sure everything we need exists
            applicationSetup()

            registerConfigModules()

            // Load config API
            DI.global.addImport(ConfigKodeinModule().create())
            // Load Android compatibility dependencies
            AndroidCompatInitializer().init()
            // start app
            androidCompat.startApp(App())

            if (args.size < 2) {
                throw RuntimeException("Inspector must be given the path of apks directory and ")
            }

            val apksPath = args[0]
            val outputPath = args[1]

            File(apksPath).list().forEach {
                println("install $it")
                installAPK(it, apksPath)
            }

            File(outputPath).printWriter().use { out ->
                getSourceList().forEach {
                    out.println("${it.id} | ${it.lang} | ${it.name}")
                }
            }
        }
    }
}
