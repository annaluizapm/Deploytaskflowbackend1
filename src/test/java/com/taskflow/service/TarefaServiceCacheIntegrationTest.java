package com.taskflow.service;

import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;
import com.taskflow.entity.Tarefa;
import com.taskflow.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TarefaServiceCacheIntegrationTest {

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TarefaRepository tarefaRepository;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
            }
        });
        tarefaRepository.deleteAll();
    }

    @Test
    void devePopularCacheDaListaEInvalidarAoCriarNovaTarefa() {
        tarefaService.listarTodas();

        Cache cache = cacheManager.getCache("tarefasListaCompleta");
        assertThat(cache).isNotNull();
        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();

        tarefaService.criar(Tarefa.builder()
                .titulo("Nova tarefa")
                .descricao("Descricao")
                .status(StatusTarefa.PENDENTE)
                .prioridade(PrioridadeTarefa.MEDIA)
                .build());

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();

        List<Tarefa> tarefas = tarefaService.listarTodas();
        assertThat(tarefas).extracting(Tarefa::getTitulo).contains("Nova tarefa");
    }
}
