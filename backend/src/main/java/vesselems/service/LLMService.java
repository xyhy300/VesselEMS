package vesselems.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vesselems.model.Model;

@Service
public class LLMService {

    private final RestTemplate rest = new RestTemplate();

    public String chat(Model model, String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));
        return doRequest(model, body);
    }

    @SuppressWarnings("unchecked")
    public String chatMulti(Model model, List<Map<String, String>> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());
        body.put("messages", messages);
        return doRequest(model, body);
    }

    @SuppressWarnings("unchecked")
    private String doRequest(Model model, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + model.getApiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map<String, Object> resp = rest.postForObject(
                model.getApiUrl() + "/chat/completions", entity, Map.class);

        if (resp == null) {
            throw new RuntimeException("大模型无响应");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("大模型返回无choices");
        }

        Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
        if (msg == null) {
            throw new RuntimeException("大模型返回无message");
        }

        return (String) msg.get("content");
    }
}