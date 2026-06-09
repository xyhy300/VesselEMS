package vesselems.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Datasource;
import vesselems.repository.DatasourceRepository;
import vesselems.service.DSManager;
import vesselems.service.SchemaService;

@RestController
@RequestMapping("/api/ds")
public class DatasourceController {

    private final DatasourceRepository repo;
    private final DSManager dsManager;
    private final SchemaService schemaService;

    public DatasourceController(DatasourceRepository repo, DSManager dsManager, SchemaService schemaService) {
        this.repo = repo;
        this.dsManager = dsManager;
        this.schemaService = schemaService;
    }

    @GetMapping
    public ApiResponse<List<Datasource>> list() {
        return ApiResponse.success(repo.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Datasource> get(@PathVariable Long id) {
        return ApiResponse.success(repo.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在")));
    }

    @PostMapping
    public ApiResponse<Datasource> create(@RequestBody Datasource ds) {
        ds.setStatus(ds.getStatus() != null ? ds.getStatus() : 1);
        ds.setCreateTime(LocalDateTime.now());
        return ApiResponse.success(repo.save(ds));
    }

    @PutMapping("/{id}")
    public ApiResponse<Datasource> update(@PathVariable Long id, @RequestBody Datasource ds) {
        Datasource exist = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        if (ds.getName() != null) exist.setName(ds.getName());
        if (ds.getUrl() != null) exist.setUrl(ds.getUrl());
        if (ds.getUsername() != null) exist.setUsername(ds.getUsername());
        if (ds.getPassword() != null) exist.setPassword(ds.getPassword());
        if (ds.getStatus() != null) exist.setStatus(ds.getStatus());
        dsManager.evict(id);
        return ApiResponse.success(repo.save(exist));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dsManager.evict(id);
        repo.deleteById(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Boolean> test(@PathVariable Long id) {
        Datasource ds = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        return ApiResponse.success(dsManager.test(ds));
    }

    @GetMapping("/{id}/schema")
    public ApiResponse<List<Map<String, Object>>> schema(@PathVariable Long id) {
        Datasource ds = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        return ApiResponse.success(schemaService.getSchema(dsManager.get(ds)));
    }
}