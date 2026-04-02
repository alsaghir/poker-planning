package com.github.alsaghir.pokerplanning.domain

import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface ThemeRepo {
    suspend fun getTheme(): ThemeDto
    suspend fun saveTheme(theme: ThemeDto)
}

class ThemeRepoImpl(
    private val storage: Storage,
    defaultColor: Color,
    private val json: Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }
) : ThemeRepo {
    private val themeKey = "theme"
    private val defaultTheme = ThemeDto.default(defaultColor)

    // Called by the ViewModel to initiate the async fetch
    override suspend fun getTheme(): ThemeDto {
        val savedString = storage.getString(themeKey)
        return if (savedString != null) {
            json.decodeFromString<ThemeDto>(savedString)
        } else {
            defaultTheme
        }

    }

    override suspend fun saveTheme(theme: ThemeDto) {
        saveToStorage(theme)
    }


    private suspend fun saveToStorage(theme: ThemeDto) {
        val serialized = json.encodeToString(theme)
        storage.putString(themeKey, serialized)
    }
}


@Serializable
enum class SerializablePaletteStyle {
    TONAL_SPOT,
    NEUTRAL,
    VIBRANT,
    EXPRESSIVE;

    fun toLibraryEnum(): PaletteStyle = when (this) {
        TONAL_SPOT -> PaletteStyle.TonalSpot
        NEUTRAL -> PaletteStyle.Neutral
        VIBRANT -> PaletteStyle.Vibrant
        EXPRESSIVE -> PaletteStyle.Expressive
    }

    companion object {
        fun fromLibraryEnum(style: PaletteStyle): SerializablePaletteStyle = when (style) {
            PaletteStyle.TonalSpot -> TONAL_SPOT
            PaletteStyle.Neutral -> NEUTRAL
            PaletteStyle.Vibrant -> VIBRANT
            PaletteStyle.Expressive -> EXPRESSIVE
            else -> error("Unsupported PaletteStyle: $style")
        }
    }
}

// Always handle changing of modes in that order
// DSL = DARK > SYSTEM > LIGHT circuling back to DARK from LIGHT and so on
@Serializable
enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}


@Serializable
data class ThemeDto(
    val colorValue: ULong,
    val paletteStyle: SerializablePaletteStyle,
    val mode: ThemeMode
) {

    fun toColor(): Color = Color(colorValue)


    fun toLibraryPaletteStyle(): PaletteStyle = paletteStyle.toLibraryEnum()

    companion object {

        fun fromColor(
            color: Color,
            paletteStyle: PaletteStyle,
            mode: ThemeMode
        ): ThemeDto {
            return ThemeDto(
                colorValue = color.value,
                paletteStyle = SerializablePaletteStyle.fromLibraryEnum(paletteStyle),
                mode = mode
            )
        }


        fun default(defaultColor: Color): ThemeDto {
            return fromColor(
                color = defaultColor,
                paletteStyle = PaletteStyle.Expressive,
                mode = ThemeMode.DARK
            )
        }
    }
}