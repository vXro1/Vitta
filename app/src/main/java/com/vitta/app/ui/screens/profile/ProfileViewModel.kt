package com.vitta.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.LogroDto
import com.vitta.app.data.remote.dto.PerfilDto
import com.vitta.app.data.repository.DashboardResult
import com.vitta.app.data.repository.LogroRepository
import com.vitta.app.data.repository.LogrosResult
import com.vitta.app.data.repository.PerfilResult
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.components.progress.Insignia
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val esRacha: Boolean,
    /** Avance real del backend (`progreso_actual` / `progreso_meta`); null si el logro no lo informa. */
    val progresoActual: Int? = null,
    val progresoMeta: Int? = null
) {
    /** true si hay datos reales para pintar la barra "3 de 7". */
    val tieneProgreso: Boolean get() = progresoActual != null && progresoMeta != null && progresoMeta > 0

    /** Avance relativo 0–1; null si el backend no informa el progreso. */
    val progresoRelativo: Float?
        get() = if (tieneProgreso) (progresoActual!!.toFloat() / progresoMeta!!).coerceIn(0f, 1f) else null
}

data class ProfileUiState(
    val cargandoLogros: Boolean = true,
    val errorLogros: String? = null,
    val logros: List<LogroUi> = emptyList(),
    /** Perfil real (`GET /api/users/me`): hábitos activos, medallas obtenidas, puntos y fecha de alta. */
    val cargandoPerfil: Boolean = true,
    val errorPerfil: String? = null,
    val perfil: PerfilDto? = null,
    /** Mejor racha histórica entre los hábitos (`GET /api/dashboard` → `rachas.maxima`). */
    val mejorRacha: Int? = null
) {
    val obtenidos: Int get() = logros.count { it.obtenido }
    val constancia: List<LogroUi> get() = logros.filter { it.esRacha }
    val areas: List<LogroUi> get() = logros.filterNot { it.esRacha }

    /** "Última medalla" del mockup: la obtenida más recientemente. */
    val ultima: LogroUi? get() = logros.filter { it.obtenido }.maxByOrNull { it.obtenidoEn.orEmpty() }

    /**
     * "La más cercana": la medalla pendiente con mayor progreso relativo
     * (`progreso_actual / progreso_meta`); a igual avance, la que menos le
     * falta y luego el orden del catálogo. Si el backend no informa progreso
     * de ninguna, se usa la primera bloqueada.
     */
    val masCercana: LogroUi?
        get() {
            val pendientes = logros.filter { !it.obtenido }
            return pendientes.filter { it.tieneProgreso }
                .sortedWith(
                    compareByDescending<LogroUi> { it.progresoRelativo }
                        .thenBy { it.progresoMeta!! - it.progresoActual!! }
                )
                .firstOrNull()
                ?: pendientes.firstOrNull()
        }
}

/**
 * Datos de las pestañas «Logros» (catálogo de medallas) y «Yo» (perfil y
 * estadísticas). Ambas pestañas comparten esta instancia.
 */
class ProfileViewModel(
    private val repository: LogroRepository = LogroRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Se llama al abrir la pestaña "Logros" para reflejar medallas recién ganadas. */
    fun cargarLogros() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoLogros = true, errorLogros = null) }
            val r = repository.listLogros()
            _uiState.update {
                when (r) {
                    is LogrosResult.Success -> it.copy(cargandoLogros = false, logros = r.logros.map { l -> l.toUi() })
                    is LogrosResult.Error -> it.copy(cargandoLogros = false, errorLogros = r.message)
                }
            }
        }
    }

    /** Se llama al abrir "Yo": estadísticas reales del backend. */
    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoPerfil = true, errorPerfil = null) }
            val dashboardCall = async { userRepository.getDashboard() }
            val perfil = userRepository.getPerfil()
            val dashboard = (dashboardCall.await() as? DashboardResult.Success)?.dashboard
            _uiState.update {
                when (perfil) {
                    is PerfilResult.Success -> it.copy(
                        cargandoPerfil = false,
                        perfil = perfil.perfil,
                        mejorRacha = dashboard?.rachas?.maxima?.dias ?: if (dashboard != null) 0 else it.mejorRacha
                    )
                    is PerfilResult.Error -> it.copy(cargandoPerfil = false, errorPerfil = perfil.message)
                }
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
        esRacha = key.startsWith("racha_"),
        progresoActual = progreso_actual,
        progresoMeta = progreso_meta
    )
}

/**
 * El backend identifica cada logro con un código en `icono` (`catalogos.sql`);
 * aquí se asocia con la misma ilustración que usa el mockup en `MED_RACHA` / `MED_AREA`.
 */
internal fun insigniaPara(icono: String): Insignia = when (icono) {
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
