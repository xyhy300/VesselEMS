package vesselems.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Dialog;
import vesselems.repository.DialogRepository;
import vesselems.service.NL2SQLService;

@RestController
@RequestMapping("/api/dialog")
public class DialogController {

    private final DialogRepository repo;
    private final NL2SQLService nl2sqlService;

    public DialogController(DialogRepository repo, NL2SQLService nl2sqlService) {
        this.repo = repo;
        this.nl2sqlService = nl2sqlService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        Long dsId = Long.valueOf(body.get("dsId").toString());
        Long modelId = Long.valueOf(body.get("modelId").toString());
        String question = (String) body.get("question");
        String sessionId = (String) body.get("sessionId");
        return ApiResponse.success(nl2sqlService.query(sessionId, dsId, modelId, question));
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<Map<String, Object>> execute(@PathVariable Long id) {
        return ApiResponse.success(nl2sqlService.execute(id));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<Map<String, Object>>> sessions() {
        return ApiResponse.success(nl2sqlService.getSessions());
    }

    @GetMapping("/session/{sessionId}")
    public ApiResponse<List<Dialog>> sessionDialogs(@PathVariable String sessionId) {
        return ApiResponse.success(nl2sqlService.getSessionDialogs(sessionId));
    }

    @GetMapping
    public ApiResponse<List<Dialog>> list() {
        return ApiResponse.success(repo.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Dialog> get(@PathVariable Long id) {
        return ApiResponse.success(repo.findById(id).orElseThrow(() -> new IllegalArgumentException("记录不存在")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ApiResponse.success(null);
    }
}