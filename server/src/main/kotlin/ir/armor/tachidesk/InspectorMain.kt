package ir.armor.tachidesk

import ir.armor.tachidesk.impl.Extension.installAPK
import ir.armor.tachidesk.impl.Source.getSourceList
import kotlinx.coroutines.runBlocking
import java.io.File

/*
 * Copyright (C) Contributors to the Suwayomi project
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object InspectorMain {
    fun inspectorMain(args: Array<String>) {
        if (args.size < 2) {
            throw RuntimeException("Inspector must be given the path of apks directory and ")
        }

        val apksPath = args[0]
        val outputPath = args[1]

        File(apksPath).list().forEach {
            println("Installing $it")
            runBlocking {
                installAPK {
                    File("$apksPath/$it").absolutePath
                }
            }
        }

        File(outputPath).printWriter().use { out ->
            getSourceList().forEach {
                out.println("${it.id} | ${it.lang} | ${it.name}")
            }
        }
    }
}
