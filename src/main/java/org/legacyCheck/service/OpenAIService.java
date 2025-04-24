package org.legacyCheck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.legacyCheck.config.BaseConfig;
import org.legacyCheck.config.PromptConfig;

import java.io.IOException;

public class OpenAIService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String analyzeCode(String cobolcode) {
        if (BaseConfig.OPENAI_KEY == null || BaseConfig.OPENAI_KEY.isBlank()) {
            System.err.println("Chave não encontrada");
            return "";
        }
        String jsonRequest = "{"
                + "\"model\": \"gpt-4\","
                + "\"messages\": ["
                + "    {\"role\": \"user\", \"content\": \"" + PromptConfig.PROMPT_01 + "\"}"
                + "],"
                + "\"temperature\": 0.3,"
                + "\"max_tokens\": 1000"
                + "}";
        RequestBody body = RequestBody.create(jsonRequest,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BaseConfig.OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + BaseConfig.OPENAI_KEY)
                .addHeader("Content-Type", "application/json")
                .build();
        try(Response response = client.newCall(request).execute()){
            if(!response.isSuccessful()){
                System.err.println("Erro de resposta: " + response.code());
                return "";
            }
            JsonNode json = mapper.readTree(response.body().string());
            return json.get("choices").get(0).get("message").get("content").asText();
        }catch (IOException e){
            System.err.println("Erro ao se comunicar com a IA" + e.getMessage());
            return "";
        }
    }
}
