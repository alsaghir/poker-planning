package com.github.alsaghir.pokerplanning.domain

import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ThemeRepo(
    private val storage: Storage,
    private val defaultColor: Color,
    private val json: Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    },
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val themeKey = "theme"

    private val defaultTheme = ThemeDto.default(defaultColor)

    private val _themeStateFlow = MutableStateFlow(loadFromStorage())

    suspend fun saveTheme(theme: ThemeDto): Result<Unit> = withContext(dispatcher) {
        try {
            saveToStorage(theme)
            _themeStateFlow.value = theme
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeTheme(): StateFlow<ThemeDto> = _themeStateFlow.asStateFlow()

    private fun loadFromStorage(): ThemeDto {
        val savedString = storage.getString(themeKey) ?: return defaultTheme

        return try {
            json.decodeFromString<ThemeDto>(savedString)
        } catch (e: Exception) {
            Logger.e("Error loading theme from storage", e)
            defaultTheme
        }
    }

    private fun saveToStorage(theme: ThemeDto) {

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
                mode = ThemeMode.SYSTEM
            )
        }
    }
}