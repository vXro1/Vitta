package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.PerfilDto

class GamificationRepository {

    suspend fun dashboard(): Result<DashboardDto> =
        runCatching { ApiClient.gamificationApi.getDashboard() }

    suspend fun perfil(): Result<PerfilDto> =
        runCatching { ApiClient.gamificationApi.getPerfil() }
}
