package io.github.shek014.openlift.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.shek014.openlift.R

/** The sounds the app can play. These names are saved in settings, so renaming one loses that choice. */
enum class Sound(val label: String, val resId: Int?) {
    None("None", null),
    Chime("Chime", R.raw.sound_chime),
    Fanfare("Fanfare", R.raw.sound_fanfare),
    Bell("Bell", R.raw.sound_bell),
    Ding("Ding", R.raw.sound_ding),
    Beep("Beep", R.raw.sound_beep),
    TripleBeep("Triple beep", R.raw.sound_triple_beep),
}

/** Plays short sounds straight away. They follow the phone's notification volume and silent mode. */
class SoundPlayer(private val context: Context) {
    private val loaded = mutableMapOf<Sound, Int>()
    private val ready = mutableSetOf<Int>()
    private val waiting = mutableSetOf<Int>()

    private val pool: SoundPool by lazy {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
            .apply {
                // The first time a sound plays it has to load first, so it plays once that's done.
                setOnLoadCompleteListener { pool, soundId, status ->
                    synchronized(this@SoundPlayer) {
                        if (status == 0) ready += soundId
                        if (waiting.remove(soundId) && status == 0) pool.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }
    }

    @Synchronized
    fun play(sound: Sound) {
        val resId = sound.resId ?: return
        val soundId = loaded.getOrPut(sound) { pool.load(context, resId, 1) }
        if (soundId in ready) pool.play(soundId, 1f, 1f, 1, 0, 1f) else waiting += soundId
    }
}
