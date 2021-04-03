package ir.armor.tachidesk.impl.backup.models

import eu.kanade.tachiyomi.source.model.SManga
// import tachiyomi.source.model.MangaInfo

interface Manga : SManga {

    var id: Long?

    var source: Long

    /** is in library */
    var favorite: Boolean

    var last_update: Long

    var date_added: Long

    var viewer: Int

    var chapter_flags: Int

    var cover_last_modified: Long

    fun setChapterOrder(order: Int) {
        setFlags(order, SORT_MASK)
    }

    fun sortDescending(): Boolean {
        return chapter_flags and SORT_MASK == SORT_DESC
    }

    fun getGenres(): List<String>? {
        return genre?.split(", ")?.map { it.trim() }
    }

    private fun setFlags(flag: Int, mask: Int) {
        chapter_flags = chapter_flags and mask.inv() or (flag and mask)
    }

    // Used to display the chapter's title one way or another
    var displayMode: Int
        get() = chapter_flags and DISPLAY_MASK
        set(mode) = setFlags(mode, DISPLAY_MASK)

    var readFilter: Int
        get() = chapter_flags and READ_MASK
        set(filter) = setFlags(filter, READ_MASK)

    var downloadedFilter: Int
        get() = chapter_flags and DOWNLOADED_MASK
        set(filter) = setFlags(filter, DOWNLOADED_MASK)

    var bookmarkedFilter: Int
        get() = chapter_flags and BOOKMARKED_MASK
        set(filter) = setFlags(filter, BOOKMARKED_MASK)

    var sorting: Int
        get() = chapter_flags and SORTING_MASK
        set(sort) = setFlags(sort, SORTING_MASK)

    companion object {

        const val SORT_DESC = 0x00000000
        const val SORT_ASC = 0x00000001
        const val SORT_MASK = 0x00000001

        // Generic filter that does not filter anything
        const val SHOW_ALL = 0x00000000

        const val SHOW_UNREAD = 0x00000002
        const val SHOW_READ = 0x00000004
        const val READ_MASK = 0x00000006

        const val SHOW_DOWNLOADED = 0x00000008
        const val SHOW_NOT_DOWNLOADED = 0x00000010
        const val DOWNLOADED_MASK = 0x00000018

        const val SHOW_BOOKMARKED = 0x00000020
        const val SHOW_NOT_BOOKMARKED = 0x00000040
        const val BOOKMARKED_MASK = 0x00000060

        const val SORTING_SOURCE = 0x00000000
        const val SORTING_NUMBER = 0x00000100
        const val SORTING_UPLOAD_DATE = 0x00000200
        const val SORTING_MASK = 0x00000300

        const val DISPLAY_NAME = 0x00000000
        const val DISPLAY_NUMBER = 0x00100000
        const val DISPLAY_MASK = 0x00100000

        fun create(source: Long): Manga = MangaImpl().apply {
            this.source = source
        }

        fun create(pathUrl: String, title: String, source: Long = 0): Manga = MangaImpl().apply {
            url = pathUrl
            this.title = title
            this.source = source
        }
    }
}

// fun Manga.toMangaInfo(): MangaInfo {
//    return MangaInfo(
//        artist = this.artist ?: "",
//        author = this.author ?: "",
//        cover = this.thumbnail_url ?: "",
//        description = this.description ?: "",
//        genres = this.getGenres() ?: emptyList(),
//        key = this.url,
//        status = this.status,
//        title = this.title
//    )
// }
