package com.vitta.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.buttons.VittaBackButton
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow

private val R24 = RoundedCornerShape(24.dp)

private data class HelpTopic(val titulo: String, val pasos: List<String>)

/** Guía de uso de cada función de la app (Yo → Ayuda). */
private val GuiaVitta = listOf(
    HelpTopic(
        "Crear un hábito",
        listOf(
            "Ve a Yo → «Agregar un hábito», o toca «Nuevo hábito» en Inicio.",
            "Elige uno o varios hábitos sugeridos y toca «Continuar», o toca «Crear un hábito propio» para inventar el tuyo.",
            "Define la meta (cantidad y unidad, por ejemplo 8 vasos o 20 minutos), los días en que toca y la hora del recordatorio.",
            "Guarda: el hábito queda en tu cuenta y aparece en Inicio y en Hoy. Crear un hábito todavía no da VitaPuntos."
        )
    ),
    HelpTopic(
        "Inicio",
        listOf(
            "Arriba ves tu nivel, tu racha más larga activa y tus VitaPuntos.",
            "En «Hoy» están los hábitos que tocan hoy. Registra el avance con los vasos, con «+5», «+1k», «Todos» o «Listo».",
            "Los hábitos que no tocan hoy aparecen en «Otros días»; también puedes registrarlos.",
            "Toca el nombre de un hábito para editarlo."
        )
    ),
    HelpTopic(
        "Hoy",
        listOf(
            "Es tu checklist del día, ordenada por mañana, tarde y noche según la hora del recordatorio de cada hábito.",
            "Un toque en el círculo completa el hábito de una vez.",
            "La acción de la derecha suma avance parcial («+5», «+1k») o lo completa («Todos», «Listo»).",
            "Arriba ves cuántos hábitos llevas registrados y el porcentaje del día."
        )
    ),
    HelpTopic(
        "Avance parcial",
        listOf(
            "En los hábitos con meta numérica puedes registrar por partes: cada toque se guarda en tu cuenta al momento.",
            "Si cierras y vuelves a abrir la app, el avance del día sigue ahí.",
            "El hábito solo cuenta como completado cuando llegas a la meta."
        )
    ),
    HelpTopic(
        "Rachas",
        listOf(
            "Cada día programado que completas suma un día a la racha de ese hábito.",
            "Los días que no tocan según la frecuencia no cortan la racha.",
            "El límite para completar un hábito es el final de su día (medianoche, en tu zona horaria). Si el día termina sin completarlo, la racha se pierde.",
            "Cuando eso pasa, Inicio te muestra «Racha terminada» con tu récord histórico. Tus logros y VitaPuntos no se pierden."
        )
    ),
    HelpTopic(
        "VitaPuntos y niveles",
        listOf(
            "Cada hábito completado suma 10 VitaPuntos; las medallas dan puntos extra.",
            "Con los puntos acumulados subes de nivel: Inicio, Explorador, Constante, En Equilibrio, Impulsor y Maestro de la Constancia.",
            "Tu nivel y lo que falta para el siguiente se ven en Inicio y en Yo."
        )
    ),
    HelpTopic(
        "Logros y medallas",
        listOf(
            "En la pestaña Logros ves todas las medallas. Las bloqueadas muestran su condición y una barra con tu avance (por ejemplo «3 de 7»).",
            "«La más cercana» es la medalla pendiente con mayor avance.",
            "Cuando desbloqueas una medalla, Vitta lo celebra una sola vez."
        )
    ),
    HelpTopic(
        "Progreso",
        listOf(
            "Alterna entre Semana y Mes para ver tu cumplimiento por día o por semana.",
            "El mapa de calor muestra las últimas cinco semanas: el verde más intenso es un día completo.",
            "«Por hábito» compara el cumplimiento de cada uno en el periodo."
        )
    ),
    HelpTopic(
        "Editar un hábito",
        listOf(
            "Toca el nombre del hábito en Inicio o en Hoy.",
            "Cambia el nombre, el icono, la meta, los días o la hora del recordatorio y toca «Guardar cambios».",
            "Tu historial, tus rachas y tus puntos se conservan."
        )
    ),
    HelpTopic(
        "Yo",
        listOf(
            "Ves tu nivel, tus VitaPuntos, los hábitos activos, las medallas obtenidas y tu mejor racha.",
            "Desde aquí puedes agregar un hábito, revisar tu cuenta y cerrar sesión."
        )
    )
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VittaBackButton(onClick = onBack)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Ayuda",
                    style = caprasimo(25f, 1.1f, -0.02f),
                    color = Mk.Ink,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Text(
                "Cómo usar cada parte de Vitta.",
                style = figtree(400, 14f, 1.5f),
                color = Mk.Body,
                modifier = Modifier.padding(top = 8.dp)
            )

            GuiaVitta.forEach { tema ->
                Column(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .mockShadow(R24)
                        .background(Mk.Paper, R24)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        tema.titulo,
                        style = caprasimo(17f, 1.2f, -0.015f),
                        color = Mk.Ink,
                        modifier = Modifier.semantics { heading() }
                    )
                    tema.pasos.forEachIndexed { i, paso ->
                        Row(Modifier.padding(top = 8.dp)) {
                            Text("${i + 1}.", style = figtree(700, 13.5f, 1.5f), color = Mk.Olive, modifier = Modifier.width(20.dp))
                            Text(paso, style = figtree(500, 13.5f, 1.5f), color = Mk.Body)
                        }
                    }
                }
            }
        }
    }
}
