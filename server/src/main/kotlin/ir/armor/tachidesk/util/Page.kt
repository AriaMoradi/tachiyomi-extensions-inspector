package ir.armor.tachidesk.util

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import ir.armor.tachidesk.Config
import ir.armor.tachidesk.database.table.ChapterTable
import ir.armor.tachidesk.database.table.MangaTable
import ir.armor.tachidesk.database.table.PageTable
import ir.armor.tachidesk.database.table.SourceTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File
import java.io.InputStream

fun getTrueImageUrl(page: Page, source: HttpSource): String {
    if (page.imageUrl == null) {
        page.imageUrl = source.fetchImageUrl(page).toBlocking().first()!!
    }
    return page.imageUrl!!
}

fun getPageImage(mangaId: Int, chapterId: Int, index: Int): Pair<InputStream, String> {
    val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.firstOrNull()!! }
    val source = getHttpSource(mangaEntry[MangaTable.sourceReference].value)
    val chapterEntry = transaction { ChapterTable.select { ChapterTable.id eq chapterId }.firstOrNull()!! }
    val pageEntry = transaction { PageTable.select { (PageTable.chapter eq chapterId) and (PageTable.index eq index) }.firstOrNull()!! }

    val tachiPage = Page(
        pageEntry[PageTable.index],
        pageEntry[PageTable.url],
        pageEntry[PageTable.imageUrl]
    )

    if (pageEntry[PageTable.imageUrl] == null) {
        transaction {
            PageTable.update({ (PageTable.chapter eq chapterId) and (PageTable.index eq index) }) {
                it[imageUrl] = getTrueImageUrl(tachiPage, source)
            }
        }
    }

    val saveDir = getChapterDir(mangaId, chapterId)
    File(saveDir).mkdirs()
    val fileName = index.toString()

    return getCachedResponse(saveDir, fileName) {
        source.fetchImage(tachiPage).toBlocking().first()
    }
}

fun getChapterDir(mangaId: Int, chapterId: Int): String {
    val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.firstOrNull()!! }
    val sourceId = mangaEntry[MangaTable.sourceReference].value
    val source = getHttpSource(sourceId)
    val sourceEntry = transaction { SourceTable.select { SourceTable.id eq sourceId }.firstOrNull()!! }
    val chapterEntry = transaction { ChapterTable.select { ChapterTable.id eq chapterId }.firstOrNull()!! }

    val chapterDir = when {
        chapterEntry[ChapterTable.scanlator] != null -> "${chapterEntry[ChapterTable.scanlator]}_${chapterEntry[ChapterTable.name]}"
        else -> chapterEntry[ChapterTable.name]
    }

    val mangaTitle = mangaEntry[MangaTable.title]
    val sourceName = source.toString()

    val mangaDir = "${Config.mangaRoot}/$sourceName/$mangaTitle/$chapterDir"
    // make sure dirs exist
    File(mangaDir).mkdirs()
    return mangaDir
}
