package ir.armor.tachidesk.impl.backup.legacy.serializer

import com.github.salomonbrys.kotson.typeAdapter
import com.google.gson.TypeAdapter
import ir.armor.tachidesk.impl.backup.models.MangaImpl

/**
 * JSON Serializer used to write / read [MangaImpl] to / from json
 */
object MangaTypeAdapter {

    fun build(): TypeAdapter<MangaImpl> {
        return typeAdapter {
            write {
                beginArray()
                value(it.url)
                value(it.title)
                value(it.source)
                value(it.viewer)
                value(it.chapter_flags)
                endArray()
            }

            read {
                beginArray()
                val manga = MangaImpl()
                manga.url = nextString()
                manga.title = nextString()
                manga.source = nextLong()
                manga.viewer = nextInt()
                manga.chapter_flags = nextInt()
                endArray()
                manga
            }
        }
    }
}
