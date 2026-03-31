package com.example.ideasimilarityapp
import com.example.ideasimilarityapp.ApiService


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var ideaText by remember { mutableStateOf("") }
            var resultText by remember { mutableStateOf("") }


            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.180.5.246:8005/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ApiService::class.java)

            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = ideaText,
                    onValueChange = { ideaText = it },
                    label = { Text("Enter Project Idea") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {

                    val call = api.checkIdea(IdeaRequest(ideaText))

                    call.enqueue(object : Callback<IdeaResponse> {

                        override fun onResponse(
                            call: Call<IdeaResponse>,
                            response: Response<IdeaResponse>
                        ) {
                            if (response.isSuccessful) {
                                val res = response.body()
                                resultText =
                                    "Status: ${res?.status}\n" +
                                            "Score: ${res?.similarity_score}\n" +
                                            "Similar: ${res?.most_similar_idea}"
                            }
                        }

                        override fun onFailure(
                            call: Call<IdeaResponse>,
                            t: Throwable
                        ) {
                            resultText = "Error: ${t.message}"
                        }
                    })

                }) {
                    Text("Check Similarity")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(resultText)
            }
        }
    }
}
