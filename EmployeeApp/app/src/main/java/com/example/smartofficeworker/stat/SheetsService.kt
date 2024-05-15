package com.example.smartofficeworker.stat

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
data class SheetValuesResponse(
    val values: List<List<String>>?
)
interface SheetsService {
    @GET("spreadsheets/{spreadsheetId}/values/{range}")
    fun getSheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Call<SheetValuesResponse>
}