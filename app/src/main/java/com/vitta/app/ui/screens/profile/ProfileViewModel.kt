package com.vitta.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.LogroDto
import com.vitta.app.data.repository.LogroRepository
import com.vitta.app.data.repository.LogrosResult
import com.vitta.app.ui.components.progress.Insignia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Un logro real (`GET /api/logros`) junto con la insignia SVG local que le corresponde. */
data class LogroUi(
    val id: Int,
    val nombre: String,
    val condicion: String,
    val puntosBonus: Int,
    val obtenido: Boolean,
    val obtenidoEn: String?,
    val insignia: Insignia,
    /** true = grupo "Constancia" (rachas) del mockup; false = grupo "Áreas". */
    val esRacha: Boolean
)

data class ProfileUiState(
    val cargandoLogros: Boolean = true,
    val errorLogros: String? = null,
    val logros: List<LogroUi> = emptyList()
) {
    val obtenidos: Int get() = logros.count { it.obtenido }
    val constancia: List<LogroUi> get() = logros.filter { it.esRacha }
    val areas: List<LogroUi> get() = logros.filterNot { it.esRacha }

    /** "Última medalla" del mockup: la obtenida más recientemente. */
    val ultima: LogroUi? get() = logros.filter { it.obtenido }.maxByOrNull { it.obtenidoEn.orEmpty() }

    /** Estado vacío del mockup ("La más cercana"): primer logro aún bloqueado. */
    val masCercana: LogroUi? get() = logros.firstOrNull { !it.obtenido }
}

class ProfileViewModel(
    private val repository: LogroRepository = LogroRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Se llama al abrir la pestaña "Yo" para reflejar logros recién ganados. */
    fun cargarLogros() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoLogros = true, errorLogros = null)
            _uiState.value = when (val r = repository.listLogros()) {
                is LogrosResult.Success -> _uiState.value.copy(
                    cargandoLogros = false,
                    logros = r.logros.map { it.toUi() }
                )
                is LogrosResult.Error -> _uiState.value.copy(cargandoLogros = false, errorLogros = r.message)
            }
        }
    }
}

private fun LogroDto.toUi(): LogroUi {
    val key = icono.orEmpty()
    return LogroUi(
        id = id,
        nombre = nombre,
        condicion = condicion,
        puntosBonus = puntos_bonus,
        obtenido = obtenido,
        obtenidoEn = obtenido_en,
        insignia = insigniaPara(key),
        esRacha = key.startsWith("racha_")
    )
}

/**
 * El backend identifica cada logro con un código en `icono` (`catalogos.sql`);
 * aquí se asocia con la misma ilustración que usa el mockup en `MED_RACHA` / `MED_AREA`.
 */
private fun insigniaPara(icono: String): Insignia = when (icono) {
    "racha_7" -> Insignia.Days7
    "racha_30" -> Insignia.Days30
    "racha_50" -> Insignia.Days50
    "racha_100" -> Insignia.Days100
    "primer_brote" -> Insignia.FirstHabit
    "mente_curiosa", "lector_constante" -> Insignia.Reader
    "amanecer_vita" -> Insignia.EarlyRiser
    "paso_a_paso", "cuerpo_activo" -> Insignia.Steps
    "plato_equilibrio", "en_equilibrio" -> Insignia.HealthyLife
    "alma_zen", "tiempo_para_mi" -> Insignia.Yoga
    else -> Insignia.FirstHabit
}
