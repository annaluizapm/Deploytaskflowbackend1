package com.taskflow.service;

import com.taskflow.dto.TarefaResumoResponse;
import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;
import com.taskflow.entity.Tarefa;
import com.taskflow.exception.TarefaNotFoundException;
import com.taskflow.repository.TarefaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = {"tarefasListaCompleta", "tarefasPorStatus", "tarefasResumidas"}, allEntries = true)
    public Tarefa criar(Tarefa tarefa) {
        tarefa.setId(null);
        tarefa.setDataCriacao(LocalDateTime.now());
        return tarefaRepository.save(tarefa);
    }

    @Cacheable("tarefasListaCompleta")
    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAllByOrderByDataCriacaoDescIdDesc();
    }

    @Cacheable(cacheNames = "tarefaPorId", key = "#id")
    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException(id));
    }

    @Cacheable(cacheNames = "tarefasPorStatus", key = "#status.name()")
    public List<Tarefa> listarPorStatus(StatusTarefa status) {
        return tarefaRepository.findByStatusOrderByDataCriacaoDescIdDesc(status);
    }

    @Cacheable(
            cacheNames = "tarefasResumidas",
            key = "T(String).valueOf(#status) + ':' + T(String).valueOf(#prioridade) + ':' + #page + ':' + #size"
    )
    public Page<TarefaResumoResponse> listarResumosPaginados(
            int page,
            int size,
            StatusTarefa status,
            PrioridadeTarefa prioridade
    ) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Order.desc("dataCriacao"), Sort.Order.desc("id"))
        );
        return tarefaRepository.buscarResumos(status, prioridade, pageable);
    }

    @Transactional
    @CacheEvict(cacheNames = {"tarefaPorId", "tarefasListaCompleta", "tarefasPorStatus", "tarefasResumidas"}, allEntries = true)
    public Tarefa atualizar(Long id, Tarefa novaTarefa) {
        Tarefa tarefa = buscarPorId(id);

        tarefa.setTitulo(novaTarefa.getTitulo());
        tarefa.setDescricao(novaTarefa.getDescricao());
        tarefa.setStatus(novaTarefa.getStatus());
        tarefa.setPrioridade(novaTarefa.getPrioridade());

        return tarefa;
    }

    @Transactional
    @CacheEvict(cacheNames = {"tarefaPorId", "tarefasListaCompleta", "tarefasPorStatus", "tarefasResumidas"}, allEntries = true)
    public void deletar(Long id) {
        Tarefa tarefa = buscarPorId(id);
        tarefaRepository.delete(tarefa);
    }
}
