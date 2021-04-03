package ir.armor.tachidesk.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import ir.armor.tachidesk.impl.MangaList.processEntries
import ir.armor.tachidesk.impl.util.GetHttpSource.getHttpSource
import ir.armor.tachidesk.impl.util.awaitSingle
import ir.armor.tachidesk.model.dataclass.PagedMangaListDataClass

object Search {
// TODO
    fun sourceFilters(sourceId: Long) {
        val source = getHttpSource(sourceId)
        // source.getFilterList().toItems()
    }

    suspend fun sourceSearch(sourceId: Long, searchTerm: String, pageNum: Int): PagedMangaListDataClass {
        val source = getHttpSource(sourceId)
        val searchManga = source.fetchSearchManga(pageNum, searchTerm, source.getFilterList()).awaitSingle()
        return searchManga.processEntries(sourceId)
    }

    fun sourceGlobalSearch(searchTerm: String) {
        // TODO
    }

    data class FilterWrapper(
        val type: String,
        val filter: Any
    )

/**
     * Note: Exhentai had a filter serializer (now in SY) that we might be able to steal
     */
// private fun FilterList.toFilterWrapper(): List<FilterWrapper> {
//    return mapNotNull { filter ->
//        when (filter) {
//            is Filter.Header -> FilterWrapper("Header",filter)
//            is Filter.Separator -> FilterWrapper("Separator",filter)
//            is Filter.CheckBox -> FilterWrapper("CheckBox",filter)
//            is Filter.TriState -> FilterWrapper("TriState",filter)
//            is Filter.Text -> FilterWrapper("Text",filter)
//            is Filter.Select<*> -> FilterWrapper("Select",filter)
//            is Filter.Group<*> -> {
//                val group = GroupItem(filter)
//                val subItems = filter.state.mapNotNull {
//                    when (it) {
//                        is Filter.CheckBox -> FilterWrapper("CheckBox",filter)
//                        is Filter.TriState -> FilterWrapper("TriState",filter)
//                        is Filter.Text -> FilterWrapper("Text",filter)
//                        is Filter.Select<*> -> FilterWrapper("Select",filter)
//                        else -> null
//                    } as? ISectionable<*, *>
//                }
//                subItems.forEach { it.header = group }
//                group.subItems = subItems
//                group
//            }
//            is Filter.Sort -> {
//                val group = SortGroup(filter)
//                val subItems = filter.values.map {
//                    SortItem(it, group)
//                }
//                group.subItems = subItems
//                group
//            }
//        }
//    }
// }
}
