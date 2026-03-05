package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MedViewModel : ViewModel() {
    val medList = mutableStateListOf<Med>()

    fun getAllMed() {
        MedClient.instance.getAllMed().enqueue(object : Callback<List<Med>> {
            override fun onResponse(call: Call<List<Med>>, response: Response<List<Med>>) {
                if (response.isSuccessful) {
                    medList.clear()
                    response.body()?.let { 
                        medList.addAll(it)
                        Log.d("MedAPI", "ดึงข้อมูลสำเร็จ: ${it.size} รายการ")
                    }
                } else {
                    Log.e("MedAPI", "ดึงข้อมูลล้มเหลว: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<Med>>, t: Throwable) {
                Log.e("MedAPI", "เชื่อมต่อไม่ได้: ${t.message}")
            }
        })
    }

    fun insertFullMedData(
        name: String,
        medicineType: String,
        amount: String,
        instructions: String,
        startDate: String,
        stopDate: String,
        quantity: Int,
        scheduleItems: List<Pair<String?, String>>,
        onComplete: (Boolean, String) -> Unit
    ) {
        Log.d("MedAPI", "เริ่มขั้นตอนที่ 1: สร้างยาหลัก ($name, $medicineType)")
        
        // 1. บันทึกลงตาราง med (แก้ไข: ส่ง medicineType เข้าไปในช่อง form)
        val medBody = MedRequest(name = name, form = medicineType)
        MedClient.instance.insertMed(medBody).enqueue(object : Callback<MedResponse> {
            override fun onResponse(call: Call<MedResponse>, response: Response<MedResponse>) {
                if (response.isSuccessful) {
                    val medId = response.body()?.idmed
                    if (medId != null) {
                        Log.d("MedAPI", "ขั้นตอนที่ 2: บันทึกยาสำเร็จ (ID: $medId), กำลังบันทึก Instruction...")
                        
                        // 2. บันทึกลงตาราง instruction
                        val instBody = InstructionRequest(
                            amount = amount,
                            instructions = instructions,
                            start_date = startDate,
                            stop_date = stopDate,
                            quantity = quantity,
                            form = medicineType
                        )
                        MedClient.instance.insertInstruction(medId, instBody).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("MedAPI", "ขั้นตอนที่ 3: บันทึก Instruction สำเร็จ, กำลังบันทึก Schedules จำนวน ${scheduleItems.size} รายการ...")
                                    
                                    // 3. บันทึกลงตาราง schedules
                                    if (scheduleItems.isEmpty()) {
                                        getAllMed()
                                        onComplete(true, "บันทึกสำเร็จ")
                                    } else {
                                        var completedCount = 0
                                        var hasError = false
                                        scheduleItems.forEach { item ->
                                            val schedBody = ScheduleRequest(time = item.first, type = item.second)
                                            MedClient.instance.insertSchedule(medId, schedBody).enqueue(object : Callback<Void> {
                                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                    completedCount++
                                                    if (!response.isSuccessful) {
                                                        hasError = true
                                                        Log.e("MedAPI", "บันทึก Schedule ล้มเหลว: ${response.code()}")
                                                    }
                                                    if (completedCount == scheduleItems.size) {
                                                        getAllMed()
                                                        onComplete(true, if (hasError) "บันทึกสำเร็จ แต่อาจมีบางเวลาที่ขัดข้อง" else "บันทึกสำเร็จครบทุกส่วน")
                                                    }
                                                }
                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                    completedCount++
                                                    hasError = true
                                                    Log.e("MedAPI", "เชื่อมต่อ Schedule ล้มเหลว: ${t.message}")
                                                    if (completedCount == scheduleItems.size) {
                                                        getAllMed()
                                                        onComplete(true, "บันทึกยาสำเร็จ แต่อาจมีบางเวลาที่ขัดข้อง")
                                                    }
                                                }
                                            })
                                        }
                                    }
                                } else {
                                    onComplete(false, "ล้มเหลวที่ตาราง Instruction (${response.code()})")
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                onComplete(false, "เกิดข้อผิดพลาดในการเชื่อมต่อ (Instruction)")
                            }
                        })
                    } else {
                        onComplete(false, "เซิร์ฟเวอร์ไม่ได้ส่ง ID ยากลับมา (กรุณาเช็ค insertId ใน Backend)")
                    }
                } else {
                    onComplete(false, "บันทึกยาหลักล้มเหลว (${response.code()})")
                }
            }
            override fun onFailure(call: Call<MedResponse>, t: Throwable) {
                onComplete(false, "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ (Med)")
            }
        })
    }

    fun deleteMed(id: Int) {
        MedClient.instance.deleteMed(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) getAllMed()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }
}
