package com.vitta.app.ui.components.icons

import androidx.annotation.DrawableRes
import com.vitta.app.R
import java.text.Normalizer

/**
 * Catálogo de iconos de hábitos.
 *
 * Todos los iconos son de Icons8, estilo "Color", PNG de 96 px descargados
 * con la licencia gratuita (requiere atribución con enlace a icons8.com, que
 * está en la sección "Yo" → Créditos; ver README_RECURSOS.md). La licencia
 * gratuita NO permite modificar los archivos (ni recolorearlos), por eso se
 * muestran tal cual, sin tint.
 *
 * Los archivos viven en `res/drawable-nodpi/ic8_*.png`: son pocos KB cada
 * uno y `painterResource` los carga sin red ni Coil.
 */
data class HabitIcon(
    val id: String,
    val label: String,
    @DrawableRes val drawable: Int,
    val category: HabitIconCategory,
    /** Palabras (sin tildes, en minúscula) para la búsqueda y para deducir el icono por nombre. */
    val keywords: List<String> = emptyList()
)

enum class HabitIconCategory(val label: String) {
    Exercise("Ejercicio"),
    Mind("Mente y descanso"),
    Health("Salud"),
    Food("Alimentación"),
    Hydration("Hidratación y bebidas"),
    Study("Estudio y lectura"),
    Work("Trabajo"),
    Organization("Organización"),
    Leisure("Tiempo libre"),
    SelfCare("Cuidado personal"),
    Home("Hogar y naturaleza"),
    Social("Familia y social"),
    Finance("Finanzas")
}

private fun icon(
    id: String,
    label: String,
    @DrawableRes drawable: Int,
    category: HabitIconCategory,
    vararg keywords: String
) = HabitIcon(id, label, drawable, category, keywords.toList())

object HabitIcons {
    // Accesos directos usados por el catálogo de hábitos predefinidos.
    val Water = icon("water", "Agua", R.drawable.ic8_water, HabitIconCategory.Hydration, "agua", "hidratar", "tomar agua", "beber")
    val Sleep = icon("sleep", "Dormir", R.drawable.ic8_sleeping_in_bed, HabitIconCategory.Mind, "dormir", "sueno", "descanso", "descansar", "siesta")
    val Walk = icon("walk", "Caminar", R.drawable.ic8_walking, HabitIconCategory.Exercise, "caminar", "camina", "pasos", "paseo")
    val Yoga = icon("yoga", "Yoga", R.drawable.ic8_yoga, HabitIconCategory.Exercise, "yoga", "flexibilidad", "postura")
    val Read = icon("read", "Leer", R.drawable.ic8_reading, HabitIconCategory.Study, "leer", "lectura", "lee")
    val Eat = icon("eat", "Comer saludable", R.drawable.ic8_healthy_food, HabitIconCategory.Food, "comer", "comida", "saludable", "aliment", "dieta", "nutricion")
    val Fruit = icon("fruit", "Fruta", R.drawable.ic8_apple, HabitIconCategory.Food, "fruta", "manzana", "frutas")
    val Meditate = icon("meditate", "Meditar", R.drawable.ic8_meditation_guru, HabitIconCategory.Mind, "medita", "meditar", "mindfulness", "calma", "respirar")

    /** Icono neutro para hábitos cuyo nombre no coincide con nada del catálogo. */
    val Default = icon("goal", "Meta", R.drawable.ic8_positive_dynamic, HabitIconCategory.Organization, "meta", "objetivo", "progreso", "mejorar")

    val all: List<HabitIcon> = listOf(
        // ---- Ejercicio ----
        Yoga,
        Walk,
        icon("running", "Correr", R.drawable.ic8_running, HabitIconCategory.Exercise, "correr", "trotar", "running", "cardio"),
        icon("stretching", "Estirar", R.drawable.ic8_stretching, HabitIconCategory.Exercise, "estirar", "estiramiento", "flexibilidad"),
        icon("exercise", "Ejercicio", R.drawable.ic8_exercise, HabitIconCategory.Exercise, "ejercicio", "entrenar", "entrena", "deporte", "actividad fisica"),
        icon("pilates", "Pilates", R.drawable.ic8_pilates, HabitIconCategory.Exercise, "pilates", "abdominales", "core"),
        icon("dumbbell", "Pesas", R.drawable.ic8_dumbbell, HabitIconCategory.Exercise, "pesas", "gym", "gimnasio", "fuerza", "musculo"),
        icon("jump_rope", "Saltar la cuerda", R.drawable.ic8_jump_rope, HabitIconCategory.Exercise, "cuerda", "saltar", "lazo"),
        icon("treadmill", "Caminadora", R.drawable.ic8_treadmill, HabitIconCategory.Exercise, "caminadora", "trotadora", "cinta", "gym"),
        icon("bicycle", "Bicicleta", R.drawable.ic8_bicycle, HabitIconCategory.Exercise, "bicicleta", "bici", "ciclismo", "pedalear"),
        icon("swimming", "Nadar", R.drawable.ic8_swimming, HabitIconCategory.Exercise, "nadar", "natacion", "piscina"),
        icon("trekking", "Senderismo", R.drawable.ic8_trekking, HabitIconCategory.Exercise, "senderismo", "caminata", "montana", "excursion"),
        icon("dancing", "Bailar", R.drawable.ic8_dancing, HabitIconCategory.Exercise, "bailar", "baile", "danza"),
        icon("climbing", "Escalar", R.drawable.ic8_climbing, HabitIconCategory.Exercise, "escalar", "escalada"),
        icon("boxing", "Boxeo", R.drawable.ic8_boxing, HabitIconCategory.Exercise, "boxeo", "boxear"),
        icon("rowing", "Remo", R.drawable.ic8_rowing, HabitIconCategory.Exercise, "remo", "remar"),
        icon("surfing", "Surf", R.drawable.ic8_surfing, HabitIconCategory.Exercise, "surf", "surfear"),
        icon("skateboarding", "Patineta", R.drawable.ic8_skateboarding, HabitIconCategory.Exercise, "patineta", "skate", "patinar"),
        icon("football", "Fútbol", R.drawable.ic8_football, HabitIconCategory.Exercise, "futbol", "balon"),
        icon("volleyball", "Voleibol", R.drawable.ic8_volleyball, HabitIconCategory.Exercise, "voleibol", "voley"),
        icon("tennis", "Tenis", R.drawable.ic8_tennis, HabitIconCategory.Exercise, "tenis", "raqueta"),
        icon("ping_pong", "Ping pong", R.drawable.ic8_ping_pong, HabitIconCategory.Exercise, "ping pong", "tenis de mesa"),

        // ---- Mente y descanso ----
        Meditate,
        icon("guru", "Meditación", R.drawable.ic8_guru, HabitIconCategory.Mind, "meditacion", "zen", "silencio"),
        icon("lotus", "Mindfulness", R.drawable.ic8_lotus, HabitIconCategory.Mind, "mindfulness", "loto", "paz", "atencion plena"),
        Sleep,
        icon("bed", "Cama", R.drawable.ic8_bed, HabitIconCategory.Mind, "cama", "acostarse", "tender la cama"),
        icon("night", "Noche", R.drawable.ic8_night, HabitIconCategory.Mind, "noche", "rutina nocturna", "desconectar"),
        icon("moon", "Luna", R.drawable.ic8_moon_symbol, HabitIconCategory.Mind, "luna", "dormir temprano"),
        icon("sunrise", "Madrugar", R.drawable.ic8_sunrise, HabitIconCategory.Mind, "madrugar", "amanecer", "levantarse temprano", "manana"),
        icon("sun", "Sol", R.drawable.ic8_sun, HabitIconCategory.Mind, "sol", "luz", "aire libre"),
        icon("relax", "Relajarse", R.drawable.ic8_relax, HabitIconCategory.Mind, "relajarse", "relax", "descansar", "pausa"),
        icon("spa", "Spa", R.drawable.ic8_spa, HabitIconCategory.Mind, "spa", "bano relajante"),
        icon("hot_springs", "Baño termal", R.drawable.ic8_hot_springs, HabitIconCategory.Mind, "termal", "jacuzzi", "relajacion"),
        icon("massage", "Masaje", R.drawable.ic8_massage, HabitIconCategory.Mind, "masaje", "fisioterapia"),
        icon("lungs", "Respirar", R.drawable.ic8_lungs, HabitIconCategory.Mind, "respirar", "respiracion", "pulmones"),
        icon("brain", "Mente", R.drawable.ic8_brain, HabitIconCategory.Mind, "mente", "cerebro", "pensar", "memoria"),
        icon("mental_health", "Salud mental", R.drawable.ic8_mental_health, HabitIconCategory.Mind, "salud mental", "terapia", "emociones", "psicologo"),
        icon("happy", "Gratitud", R.drawable.ic8_happy, HabitIconCategory.Mind, "gratitud", "feliz", "animo", "agradecer"),
        icon("smiling", "Sonreír", R.drawable.ic8_smiling, HabitIconCategory.Mind, "sonreir", "alegria", "humor"),

        // ---- Salud ----
        icon("heart_pulse", "Salud", R.drawable.ic8_heart_with_pulse, HabitIconCategory.Health, "salud", "corazon", "pulso", "cardio"),
        icon("heart_health", "Bienestar", R.drawable.ic8_heart_health, HabitIconCategory.Health, "bienestar", "cuidarme"),
        icon("health_checkup", "Chequeo médico", R.drawable.ic8_health_checkup, HabitIconCategory.Health, "chequeo", "control medico", "cita medica"),
        icon("doctor", "Médico", R.drawable.ic8_medical_doctor, HabitIconCategory.Health, "medico", "doctor", "doctora", "cita"),
        icon("stethoscope", "Estetoscopio", R.drawable.ic8_stethoscope, HabitIconCategory.Health, "estetoscopio", "consulta"),
        icon("pill", "Medicamento", R.drawable.ic8_pill, HabitIconCategory.Health, "medicamento", "pastilla", "medicina", "tomar pastilla"),
        icon("pills", "Vitaminas", R.drawable.ic8_pills, HabitIconCategory.Health, "vitaminas", "suplemento", "pastillas"),
        icon("thermometer", "Temperatura", R.drawable.ic8_thermometer, HabitIconCategory.Health, "temperatura", "termometro", "fiebre"),
        icon("scale", "Peso", R.drawable.ic8_scale, HabitIconCategory.Health, "peso", "bascula", "pesarse"),
        icon("first_aid", "Botiquín", R.drawable.ic8_doctors_bag, HabitIconCategory.Health, "botiquin", "primeros auxilios"),
        icon("tooth", "Dientes", R.drawable.ic8_tooth, HabitIconCategory.Health, "dientes", "dentista", "muela"),
        icon("no_smoking", "No fumar", R.drawable.ic8_no_smoking, HabitIconCategory.Health, "no fumar", "dejar de fumar", "cigarrillo"),

        // ---- Alimentación ----
        Eat,
        icon("salad", "Ensalada", R.drawable.ic8_salad, HabitIconCategory.Food, "ensalada", "verduras", "vegetales"),
        Fruit,
        icon("banana", "Banano", R.drawable.ic8_banana, HabitIconCategory.Food, "banano", "platano", "fruta"),
        icon("avocado", "Aguacate", R.drawable.ic8_avocado, HabitIconCategory.Food, "aguacate", "grasas saludables"),
        icon("broccoli", "Brócoli", R.drawable.ic8_broccoli, HabitIconCategory.Food, "brocoli", "verdura", "vegetales"),
        icon("carrot", "Zanahoria", R.drawable.ic8_carrot, HabitIconCategory.Food, "zanahoria", "verdura"),
        icon("vegetarian", "Vegetariano", R.drawable.ic8_vegetarian_food, HabitIconCategory.Food, "vegetariano", "vegano", "plantas"),
        icon("breakfast", "Desayuno", R.drawable.ic8_breakfast, HabitIconCategory.Food, "desayuno", "desayunar"),
        icon("lunchbox", "Almuerzo", R.drawable.ic8_lunchbox, HabitIconCategory.Food, "almuerzo", "lonchera", "comida casera"),
        icon("cooking", "Cocinar", R.drawable.ic8_cooking, HabitIconCategory.Food, "cocinar", "cocina", "preparar comida"),
        icon("cooking_pot", "Olla", R.drawable.ic8_cooking_pot, HabitIconCategory.Food, "olla", "sopa", "cocinar"),
        icon("fish", "Pescado", R.drawable.ic8_fish, HabitIconCategory.Food, "pescado", "proteina", "omega"),
        icon("egg", "Huevo", R.drawable.ic8_egg, HabitIconCategory.Food, "huevo", "proteina"),
        icon("milk", "Leche", R.drawable.ic8_milk, HabitIconCategory.Food, "leche", "lacteos", "calcio"),

        // ---- Hidratación y bebidas ----
        Water,
        icon("bottle_water", "Botella de agua", R.drawable.ic8_bottle_of_water, HabitIconCategory.Hydration, "botella", "agua", "hidratacion", "litros"),
        icon("water_bottle", "Termo", R.drawable.ic8_water_bottle, HabitIconCategory.Hydration, "termo", "botella", "agua"),
        icon("tea", "Té", R.drawable.ic8_tea, HabitIconCategory.Hydration, "te", "infusion", "aromatica"),
        icon("coffee", "Café", R.drawable.ic8_coffee, HabitIconCategory.Hydration, "cafe", "menos cafe"),
        icon("coffee_to_go", "Café para llevar", R.drawable.ic8_coffee_to_go, HabitIconCategory.Hydration, "cafe", "bebida"),
        icon("kettle", "Tetera", R.drawable.ic8_kettle, HabitIconCategory.Hydration, "tetera", "te", "agua caliente"),

        // ---- Estudio y lectura ----
        Read,
        icon("book", "Libro", R.drawable.ic8_book, HabitIconCategory.Study, "libro", "leer", "lectura"),
        icon("books", "Libros", R.drawable.ic8_books, HabitIconCategory.Study, "libros", "biblioteca", "lectura"),
        icon("open_book", "Libro abierto", R.drawable.ic8_open_book, HabitIconCategory.Study, "paginas", "leer", "novela"),
        icon("homework", "Tareas", R.drawable.ic8_homework, HabitIconCategory.Study, "tareas", "deberes", "estudiar"),
        icon("graduation", "Estudiar", R.drawable.ic8_graduation_cap, HabitIconCategory.Study, "estudiar", "estudio", "universidad", "clase", "graduacion"),
        icon("learning", "Aprender", R.drawable.ic8_learning, HabitIconCategory.Study, "aprender", "curso", "idioma"),
        icon("library", "Biblioteca", R.drawable.ic8_library, HabitIconCategory.Study, "biblioteca", "investigar"),
        icon("writing", "Escribir", R.drawable.ic8_hand_with_pen, HabitIconCategory.Study, "escribir", "escritura", "redactar"),
        icon("pen", "Pluma", R.drawable.ic8_pen, HabitIconCategory.Study, "pluma", "firmar", "caligrafia"),
        icon("journal", "Diario", R.drawable.ic8_journal, HabitIconCategory.Study, "diario", "journaling", "bitacora"),
        icon("note", "Notas", R.drawable.ic8_note, HabitIconCategory.Study, "notas", "apuntes", "resumen"),
        icon("idea", "Ideas", R.drawable.ic8_idea, HabitIconCategory.Study, "ideas", "creatividad", "pensar"),

        // ---- Trabajo ----
        icon("laptop", "Computador portátil", R.drawable.ic8_laptop, HabitIconCategory.Work, "computador", "portatil", "laptop", "trabajar"),
        icon("computer", "Computador", R.drawable.ic8_computer, HabitIconCategory.Work, "computador", "pantalla", "pc"),
        icon("workstation", "Escritorio", R.drawable.ic8_workstation, HabitIconCategory.Work, "escritorio", "puesto de trabajo"),
        icon("office", "Oficina", R.drawable.ic8_office, HabitIconCategory.Work, "oficina", "trabajo"),
        icon("briefcase", "Trabajo", R.drawable.ic8_briefcase, HabitIconCategory.Work, "trabajo", "maletin", "empleo"),
        icon("meeting", "Reuniones", R.drawable.ic8_meeting, HabitIconCategory.Work, "reunion", "reuniones", "equipo"),
        icon("presentation", "Presentación", R.drawable.ic8_presentation, HabitIconCategory.Work, "presentacion", "exponer"),
        icon("video_call", "Videollamada", R.drawable.ic8_video_call, HabitIconCategory.Work, "videollamada", "llamada", "zoom"),
        icon("email", "Correo", R.drawable.ic8_email, HabitIconCategory.Work, "correo", "email", "bandeja"),
        icon("calculator", "Calculadora", R.drawable.ic8_calculator, HabitIconCategory.Work, "calculadora", "cuentas", "matematicas"),
        icon("code", "Programar", R.drawable.ic8_code, HabitIconCategory.Work, "programar", "codigo", "programacion"),
        icon("phone", "Llamar", R.drawable.ic8_phone, HabitIconCategory.Work, "llamar", "telefono", "llamada"),
        icon("smartphone", "Celular", R.drawable.ic8_smartphone, HabitIconCategory.Work, "celular", "pantalla", "menos celular", "redes"),

        // ---- Organización ----
        Default,
        icon("tasks", "Tareas del día", R.drawable.ic8_tasks, HabitIconCategory.Organization, "pendientes", "productividad"),
        icon("todo", "Lista de pendientes", R.drawable.ic8_todo_list, HabitIconCategory.Organization, "lista", "pendientes", "to do"),
        icon("checklist", "Checklist", R.drawable.ic8_checklist, HabitIconCategory.Organization, "checklist", "rutina"),
        icon("planner", "Planificar", R.drawable.ic8_planner, HabitIconCategory.Organization, "planificar", "agenda", "semana"),
        icon("calendar", "Calendario", R.drawable.ic8_calendar, HabitIconCategory.Organization, "calendario", "fecha"),
        icon("alarm_clock", "Despertador", R.drawable.ic8_alarm_clock, HabitIconCategory.Organization, "despertador", "alarma", "levantarse"),
        icon("time", "Tiempo", R.drawable.ic8_time, HabitIconCategory.Organization, "tiempo", "cronometro", "puntualidad"),

        // ---- Tiempo libre ----
        icon("music", "Música", R.drawable.ic8_music, HabitIconCategory.Leisure, "musica", "cancion"),
        icon("headphones", "Escuchar", R.drawable.ic8_headphones, HabitIconCategory.Leisure, "audifonos", "escuchar", "podcast"),
        icon("guitar", "Guitarra", R.drawable.ic8_guitar, HabitIconCategory.Leisure, "guitarra", "instrumento", "tocar"),
        icon("piano", "Piano", R.drawable.ic8_piano, HabitIconCategory.Leisure, "piano", "instrumento", "teclado"),
        icon("microphone", "Cantar", R.drawable.ic8_microphone, HabitIconCategory.Leisure, "cantar", "microfono", "voz"),
        icon("radio", "Radio", R.drawable.ic8_radio, HabitIconCategory.Leisure, "radio", "noticias"),
        icon("movie", "Cine", R.drawable.ic8_movie, HabitIconCategory.Leisure, "cine", "pelicula", "serie"),
        icon("popcorn", "Palomitas", R.drawable.ic8_popcorn, HabitIconCategory.Leisure, "palomitas", "crispetas", "pelicula"),
        icon("tv", "Televisión", R.drawable.ic8_tv, HabitIconCategory.Leisure, "television", "tv", "menos pantalla"),
        icon("theatre", "Teatro", R.drawable.ic8_theatre_mask, HabitIconCategory.Leisure, "teatro", "actuar"),
        icon("controller", "Videojuegos", R.drawable.ic8_controller, HabitIconCategory.Leisure, "videojuegos", "jugar", "consola"),
        icon("joystick", "Juegos", R.drawable.ic8_joystick, HabitIconCategory.Leisure, "juegos", "arcade"),
        icon("board_game", "Juegos de mesa", R.drawable.ic8_board_game, HabitIconCategory.Leisure, "juegos de mesa", "ajedrez", "tablero"),
        icon("puzzle", "Rompecabezas", R.drawable.ic8_puzzle, HabitIconCategory.Leisure, "rompecabezas", "puzzle", "logica"),
        icon("drawing", "Dibujar", R.drawable.ic8_drawing, HabitIconCategory.Leisure, "dibujar", "dibujo", "bocetos"),
        icon("paint_palette", "Pintar", R.drawable.ic8_paint_palette, HabitIconCategory.Leisure, "pintar", "arte", "acuarela"),
        icon("paint_brush", "Manualidades", R.drawable.ic8_paint_brush, HabitIconCategory.Leisure, "manualidades", "pincel", "crear"),
        icon("camera", "Fotografía", R.drawable.ic8_camera, HabitIconCategory.Leisure, "fotografia", "fotos", "camara"),
        icon("kite", "Aire libre", R.drawable.ic8_kite, HabitIconCategory.Leisure, "cometa", "parque", "aire libre", "jugar"),
        icon("map", "Explorar", R.drawable.ic8_map, HabitIconCategory.Leisure, "explorar", "mapa", "viajar"),
        icon("beach", "Playa", R.drawable.ic8_beach, HabitIconCategory.Leisure, "playa", "vacaciones", "viaje"),
        icon("tent", "Camping", R.drawable.ic8_tent, HabitIconCategory.Leisure, "camping", "acampar", "naturaleza"),
        icon("fishing", "Pescar", R.drawable.ic8_fishing, HabitIconCategory.Leisure, "pescar", "pesca"),

        // ---- Cuidado personal ----
        icon("shower", "Ducha", R.drawable.ic8_shower, HabitIconCategory.SelfCare, "ducha", "banarse", "higiene", "duchar"),
        icon("bath", "Baño", R.drawable.ic8_bath, HabitIconCategory.SelfCare, "bano", "tina", "higiene"),
        icon("toothbrush", "Cepillarse", R.drawable.ic8_toothbrush, HabitIconCategory.SelfCare, "cepillar", "cepillarse", "dientes", "higiene"),
        icon("soap", "Lavarse las manos", R.drawable.ic8_soap, HabitIconCategory.SelfCare, "jabon", "manos", "lavar", "higiene"),
        icon("comb", "Peinarse", R.drawable.ic8_comb, HabitIconCategory.SelfCare, "peinarse", "peine", "cabello"),
        icon("hair_brush", "Cabello", R.drawable.ic8_hair_brush, HabitIconCategory.SelfCare, "cabello", "pelo", "cepillo"),
        icon("hair_dryer", "Secador", R.drawable.ic8_hair_dryer, HabitIconCategory.SelfCare, "secador", "arreglarse"),
        icon("nail_polish", "Uñas", R.drawable.ic8_nail_polish, HabitIconCategory.SelfCare, "unas", "manicure", "esmalte"),

        // ---- Hogar y naturaleza ----
        icon("broom", "Barrer", R.drawable.ic8_broom, HabitIconCategory.Home, "barrer", "limpiar", "aseo"),
        icon("housekeeping", "Limpieza", R.drawable.ic8_housekeeping, HabitIconCategory.Home, "limpieza", "limpiar", "aseo", "orden"),
        icon("vacuum", "Aspirar", R.drawable.ic8_vacuum_cleaner, HabitIconCategory.Home, "aspirar", "aspiradora"),
        icon("laundry", "Lavar ropa", R.drawable.ic8_washing_machine, HabitIconCategory.Home, "lavar ropa", "lavadora", "ropa"),
        icon("iron", "Planchar", R.drawable.ic8_iron, HabitIconCategory.Home, "planchar", "plancha"),
        icon("garden", "Jardinería", R.drawable.ic8_garden, HabitIconCategory.Home, "jardin", "jardineria", "sembrar"),
        icon("watering_can", "Regar plantas", R.drawable.ic8_watering_can, HabitIconCategory.Home, "regar", "plantas", "regadera"),
        icon("potted_plant", "Plantas", R.drawable.ic8_potted_plant, HabitIconCategory.Home, "planta", "plantas", "matera"),
        icon("flower", "Flores", R.drawable.ic8_flower, HabitIconCategory.Home, "flor", "flores"),
        icon("leaf", "Naturaleza", R.drawable.ic8_leaf, HabitIconCategory.Home, "naturaleza", "hoja", "ecologia", "reciclar"),
        icon("hammer", "Reparaciones", R.drawable.ic8_hammer, HabitIconCategory.Home, "reparar", "arreglar", "herramientas"),

        // ---- Familia y social ----
        icon("family", "Familia", R.drawable.ic8_family, HabitIconCategory.Social, "familia", "hijos", "padres"),
        icon("friends", "Amigos", R.drawable.ic8_friends, HabitIconCategory.Social, "amigos", "amistad"),
        icon("hug", "Abrazo", R.drawable.ic8_hug, HabitIconCategory.Social, "abrazo", "carino", "pareja"),
        icon("handshake", "Ayudar", R.drawable.ic8_handshake, HabitIconCategory.Social, "ayudar", "voluntariado", "acuerdo"),
        icon("baby", "Bebé", R.drawable.ic8_baby, HabitIconCategory.Social, "bebe", "crianza"),
        icon("elderly", "Abuelos", R.drawable.ic8_elderly_person, HabitIconCategory.Social, "abuelos", "abuela", "abuelo", "visitar"),
        icon("pet", "Pasear mascota", R.drawable.ic8_pet, HabitIconCategory.Social, "mascota", "pasear al perro", "perro"),
        icon("dog", "Perro", R.drawable.ic8_dog, HabitIconCategory.Social, "perro", "mascota"),
        icon("cat", "Gato", R.drawable.ic8_cat, HabitIconCategory.Social, "gato", "mascota"),
        icon("paw", "Mascotas", R.drawable.ic8_cat_footprint, HabitIconCategory.Social, "mascotas", "huella", "animales"),
        icon("prayer", "Orar", R.drawable.ic8_prayer, HabitIconCategory.Social, "orar", "rezar", "oracion", "espiritualidad"),

        // ---- Finanzas ----
        icon("money_box", "Ahorrar", R.drawable.ic8_money_box, HabitIconCategory.Finance, "ahorrar", "ahorro", "alcancia"),
        icon("coins", "Monedas", R.drawable.ic8_coins, HabitIconCategory.Finance, "monedas", "dinero", "gastos"),
        icon("money", "Dinero", R.drawable.ic8_money, HabitIconCategory.Finance, "dinero", "presupuesto", "finanzas"),
        icon("wallet", "Billetera", R.drawable.ic8_wallet, HabitIconCategory.Finance, "billetera", "gastar menos", "cartera")
    )

    private val byId: Map<String, HabitIcon> = all.associateBy { it.id }

    fun byId(id: String?): HabitIcon? = id?.let { byId[it] }

    /** Iconos que se muestran de entrada en el selector (antes de abrir el catálogo completo). */
    val featured: List<HabitIcon> = listOf(
        "yoga", "meditate", "running", "walk", "water", "sleep",
        "read", "eat", "dumbbell", "music", "graduation"
    ).mapNotNull { byId[it] }

    fun grouped(icons: List<HabitIcon> = all): List<Pair<HabitIconCategory, List<HabitIcon>>> =
        icons.groupBy { it.category }.toList().sortedBy { it.first.ordinal }

    /** Búsqueda insensible a mayúsculas y tildes sobre nombre y palabras clave. */
    fun search(query: String): List<HabitIcon> {
        val q = normalize(query)
        if (q.isBlank()) return all
        return all.filter { icon ->
            normalize(icon.label).contains(q) ||
                icon.keywords.any { it.contains(q) } ||
                normalize(icon.category.label).contains(q)
        }
    }

    /**
     * Deduce el icono a partir del nombre del hábito. El backend no guarda
     * el icono (ver README_RECURSOS.md → Limitaciones), así que esta es la
     * única forma de mostrar el mismo icono al volver a cargar los hábitos.
     */
    fun forHabitName(nombre: String): HabitIcon? {
        val n = normalize(nombre)
        if (n.isBlank()) return null
        // Prioridad a los accesos directos (los nombres de los predefinidos).
        val priority = listOf(Water, Sleep, Meditate, Yoga, Walk, Read, Fruit, Eat)
        priority.firstOrNull { icon -> icon.keywords.any { n.contains(it) } }?.let { return it }
        return all.firstOrNull { icon ->
            icon.keywords.any { it.length >= 4 && n.contains(it) } || n.contains(normalize(icon.label))
        }
    }

    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
}
