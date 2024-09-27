package com.example.platemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private EditText tUsername, tSenha;
    private Button buttonLogin;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tUsername = findViewById(R.id.tUsername);
        tSenha = findViewById(R.id.tSenha);
        buttonLogin = findViewById(R.id.buttonLogin);

        client = new OkHttpClient();
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        buttonLogin.setOnClickListener(v -> {
            String username = tUsername.getText().toString().trim();
            String password = tSenha.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                fazerLogin(username, password);
            }
        });
    }

    private void fazerLogin(String username, String password) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://es.eva.inf.br/eva/mobileWS/login")
                .newBuilder();
        urlBuilder.addQueryParameter("username", username);
        urlBuilder.addQueryParameter("password", password);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erro ao conectar-se: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("lojaId", jsonResponse.getString("lojaId"));
                            editor.putString("cnpj", jsonResponse.getString("cnpj"));
                            editor.putString("message", jsonResponse.getString("message"));
                            editor.apply();

                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish(); // Opcional: fecha a MainActivity
                            });
                        } else {
                            String message = jsonResponse.getString("message");
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "Falha no login: " + message, Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Erro ao processar resposta: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Falha na requisição: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}