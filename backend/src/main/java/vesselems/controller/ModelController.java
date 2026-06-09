package vesselems.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Model;
import vesselems.repository.ModelRepository;

@RestController
@RequestMapping("/api/model")
public class ModelController {

    private final ModelRepository repo;

    public ModelController(ModelRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ApiResponse<List<Model>> list() {
        return ApiResponse.success(repo.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Model> get(@PathVariable Long id) {
        return ApiResponse.success(repo.findById(id).orElseThrow(() -> new IllegalArgumentException("模型不存在")));
    }

    @PostMapping
    public ApiResponse<Model> create(@RequestBody Model m) {
        m.setStatus(m.getStatus() != null ? m.getStatus() : 1);
        m.setCreateTime(LocalDateTime.now());
        return ApiResponse.success(repo.save(m));
    }

    @PutMapping("/{id}")
    public ApiResponse<Model> update(@PathVariable Long id, @RequestBody Model m) {
        Model exist = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("模型不存在"));
        if (m.getName() != null) exist.setName(m.getName());
        if (m.getApiUrl() != null) exist.setApiUrl(m.getApiUrl());
        if (m.getApiKey() != null) exist.setApiKey(m.getApiKey());
        if (m.getModelId() != null) exist.setModelId(m.getModelId());
        if (m.getStatus() != null) exist.setStatus(m.getStatus());
        return ApiResponse.success(repo.save(exist));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ApiResponse.success(null);
    }
}