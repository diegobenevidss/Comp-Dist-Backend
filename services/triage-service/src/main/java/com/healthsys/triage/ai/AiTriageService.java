package com.healthsys.triage.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthsys.triage.triage.domain.Priority;
import com.healthsys.triage.triage.dto.SuggestPriorityRequest;
import com.healthsys.triage.triage.dto.SuggestPriorityResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiTriageService {

    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String SYSTEM_PROMPT = """
        Você é um assistente de triagem hospitalar baseado no Protocolo de Manchester.
        Classifique a prioridade de atendimento com base na queixa principal do paciente.

        Níveis disponíveis:
        - EMERGENCY: risco imediato de vida (parada cardíaca, obstrução de via aérea)
        - VERY_URGENT: risco de vida se não tratado em 10 min (AVC, dor torácica severa)
        - URGENT: situação séria com tratamento em até 60 min (fratura, dor intensa)
        - LESS_URGENT: estável, aguarda até 2h (febre baixa, dor leve)
        - NON_URGENT: não urgente, aguarda 4h+ (gripe leve, ferida pequena)

        Responda SOMENTE com JSON válido, sem markdown:
        {"priority":"<NÍVEL>","reasoning":"<justificativa em português, máx 80 palavras>"}
        """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AiTriageService(ObjectMapper objectMapper, @Value("${anthropic.api-key:}") String apiKey) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public SuggestPriorityResponse suggestPriority(SuggestPriorityRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException("Serviço de IA não configurado (ANTHROPIC_API_KEY ausente)");
        }

        String userMessage = "Queixa principal: " + request.chiefComplaint();
        if (request.notes() != null && !request.notes().isBlank()) {
            userMessage += "\nNotas: " + request.notes();
        }

        Map<String, Object> body = Map.of(
            "model", MODEL,
            "max_tokens", 256,
            "system", SYSTEM_PROMPT,
            "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        try {
            String raw = restClient.post()
                .uri(API_URL)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            String text = root.path("content").get(0).path("text").asText();
            JsonNode result = objectMapper.readTree(text);

            Priority priority = Priority.valueOf(result.path("priority").asText());
            String reasoning = result.path("reasoning").asText();
            return new SuggestPriorityResponse(priority, reasoning);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Falha ao consultar IA: " + e.getMessage());
        }
    }
}