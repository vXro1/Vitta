package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.CreateReminderRequest
import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.PerfilDto
import com.vitta.app.data.remote.dto.ReminderDto
import com.vitta.app.data.remote.dto.UpdateReminderRequest
import retrofit2.HttpException

sealed class PerfilResult {
    data class Success(val perfil: PerfilDto) : PerfilResult()
    data class Error(val message: String) : PerfilResult()
}

sealed class DashboardResult {
    data class Success(val dashboard: DashboardDto) : DashboardResult()
    data class Error(val message: String) : DashboardResult()
}

sealed class RemindersResult {
    data class Success(val reminders: List<ReminderDto>) : RemindersResult()
    data class Error(val message: String) : RemindersResult()
}

sealed class SaveReminderResult {
    data class Success(val reminder: ReminderDto) : SaveReminderResult()
    data class Error(val message: String) : SaveReminderResult()
}

class UserRepository {

    suspend fun getPerfil(): PerfilResult = try {
        PerfilResult.Success(ApiClient.userApi.getPerfil())
    } catch (e: HttpException) {
        PerfilResult.Error(
            if (e.code() == 401) "Tu sesión expiró. Vuelve a iniciar sesión."
            else "No se pudo cargar tu perfil (${e.code()})"
        )
    } catch (e: Exception) {
        PerfilResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun getDashboard(): DashboardResult = try {
        DashboardResult.Success(ApiClient.userApi.getDashboard())
    } catch (e: HttpException) {
        DashboardResult.Error("No se pudo cargar tu resumen (${e.code()})")
    } catch (e: Exception) {
        DashboardResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun listReminders(): RemindersResult = try {
        RemindersResult.Success(ApiClient.userApi.listReminders())
    } catch (e: HttpException) {
        RemindersResult.Error("No se pudieron cargar tus recordatorios (${e.code()})")
    } catch (e: Exception) {
        RemindersResult.Error("No se pudo conectar.")
    }

    /**
     * Guarda la hora (`HH:MM`) del hábito: actualiza su recordatorio si ya
     * tiene uno y si no lo crea (`/api/recordatorios`).
     */
    suspend fun saveReminder(habitId: Int, hora: String): SaveReminderResult = try {
        val existing = ApiClient.userApi.listReminders().firstOrNull { it.habito_id == habitId }
        SaveReminderResult.Success(
            if (existing == null) ApiClient.userApi.createReminder(CreateReminderRequest(habitId, hora))
            else if (existing.hora == hora) existing
            else ApiClient.userApi.updateReminder(existing.id, UpdateReminderRequest(hora))
        )
    } catch (e: HttpException) {
        SaveReminderResult.Error("No se pudo guardar la hora del recordatorio (${e.code()})")
    } catch (e: Exception) {
        SaveReminderResult.Error("No se pudo conectar para guardar la hora del recordatorio.")
    }
}
