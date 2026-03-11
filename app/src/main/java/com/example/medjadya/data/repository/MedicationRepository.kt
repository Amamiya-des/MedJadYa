package com.example.medjadya.data.repository

import android.content.Context
import com.example.medjadya.model.*
import com.example.medjadya.network.RetrofitClient
import retrofit2.Response

class MedicationRepository(context: Context) {
    private val api = RetrofitClient.getApiService(context)

    suspend fun getAllMeds(): Response<List<Medication>> {
        return api.getAllMeds()
    }

    suspend fun getMedsByUserId(userId: Int): Response<List<Medication>> {
        return api.getMedsByUserId(userId)
    }

    suspend fun insertMed(request: MedRequest): Response<MedResponse> {
        return api.insertMed(request)
    }

    suspend fun insertInstruction(medId: Int, request: InstructionRequest): Response<Unit> {
        return api.insertInstruction(medId, request)
    }

    suspend fun insertSchedule(medId: Int, request: ScheduleRequest): Response<Unit> {
        return api.insertSchedule(medId, request)
    }

    suspend fun updateInstruction(id: Int, request: UpdateInstructionRequest): Response<Unit> {
        return api.updateInstruction(id, request)
    }

    suspend fun deleteMed(id: Int): Response<Unit> {
        return api.deleteMed(id)
    }

    suspend fun getInstructions(medId: Int): List<Instruction> {
        return api.getInstructions(medId)
    }

    suspend fun getSchedules(medId: Int): List<Schedule> {
        return api.getSchedules(medId)
    }

    suspend fun logMedication(medId: Int, status: String, takenTime: String?): Response<LogResponse> {
        return api.logMedication(medId, LogStatusRequest(status, takenAt = takenTime))
    }

    suspend fun getLogsByMedId(medId: Int): List<MedLog> {
        return api.getLogsByMedId(medId)
    }
}
