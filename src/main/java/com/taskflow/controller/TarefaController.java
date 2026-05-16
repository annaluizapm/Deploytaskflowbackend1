package com.taskflow.controller;

import com.taskflow.dto.TarefaResumoResponse;
import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;
import com.taskflow.entity.Tarefa;
import com.taskflow.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
@CrossOrigin(origins = "*")
public class TarefaController {

    private static final CacheControl LIST_CACHE_CONTROL = CacheControl.maxAge(Duration.ofSeconds(20))
            .cachePrivate()
            .mustRevalidate();

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<Tarefa> criar(@Valid @RequestBody Tarefa tarefa) {
        Tarefa novaTarefa = tarefaService.criar(tarefa);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaTarefa);
    }

    @GetMapping
    public ResponseEntity<List<Tarefa>> listarTodas() {
        return ResponseEntity.ok()
                .cacheControl(LIST_CACHE_CONTROL)
                .body(tarefaService.listarTodas());
    }

    @GetMapping("/paginadas")
    public ResponseEntity<Page<TarefaResumoResponse>> listarPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) PrioridadeTarefa prioridade
    ) {
        return ResponseEntity.ok()
                .cacheControl(LIST_CACHE_CONTROL)
                .body(tarefaService.listarResumosPaginados(page, size, status, prioridade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tarefa>> listarPorStatus(@PathVariable StatusTarefa status) {
        return ResponseEntity.ok()
                .cacheControl(LIST_CACHE_CONTROL)
                .body(tarefaService.listarPorStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(@PathVariable Long id, @Valid @RequestBody Tarefa tarefa) {
        return ResponseEntity.ok(tarefaService.atualizar(id, tarefa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tarefaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
