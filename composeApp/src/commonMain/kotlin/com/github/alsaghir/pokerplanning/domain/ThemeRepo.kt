package com.github.alsaghir.pokerplanning.domain

import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ThemeRepo(
    private val storage: Storage,
    defaultColor: Color,
    private val json: Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    },
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val themeKey = "theme"

    private val defaultTheme = ThemeDto.default(defaultColor)

    // For async operations. SupervisorJob allows child coroutines to fail independently without cancelling the entire scope.
    private val repoScope = CoroutineScope(dispatcher + SupervisorJob())

    private val _themeState = MutableStateFlow<DataState<ThemeDto>>(DataState.Loading)
    val themeState: StateFlow<DataState<ThemeDto>> = _themeState.asStateFlow()

    init {
        repoScope.launch {
            _themeState.value = try {
                DataState.Success(loadFromStorage())
            } catch (e: Exception) {
                DataState.Error(e)
            }
        }
    }

    fun close() {
        repoScope.cancel()
    }

    suspend fun saveTheme(theme: ThemeDto): Result<Unit> = withContext(dispatcher) {
        try {
            saveToStorage(theme)
            _themeState.value = DataState.Success(theme)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _themeState.value = DataState.Error(e)
            Result.failure(e)
        }
    }


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