package com.taskflow.controller;

import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;
import com.taskflow.entity.Tarefa;
import com.taskflow.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
            }
        });

        tarefaRepository.deleteAll();

        tarefaRepository.save(Tarefa.builder()
                .titulo("Tarefa antiga")
                .descricao("Descricao da tarefa antiga")
                .status(StatusTarefa.PENDENTE)
                .prioridade(PrioridadeTarefa.BAIXA)
                .dataCriacao(LocalDateTime.now().minusDays(2))
                .build());

        tarefaRepository.save(Tarefa.builder()
                .titulo("Tarefa intermediaria")
                .descricao("Descricao da tarefa intermediaria")
                .status(StatusTarefa.EM_ANDAMENTO)
                .prioridade(PrioridadeTarefa.MEDIA)
                .dataCriacao(LocalDateTime.now().minusDays(1))
                .build());

        tarefaRepository.save(Tarefa.builder()
                .titulo("Tarefa recente")
                .descricao("Descricao da tarefa recente")
                .status(StatusTarefa.PENDENTE)
                .prioridade(PrioridadeTarefa.ALTA)
                .dataCriacao(LocalDateTime.now())
                .build());
    }

    @Test
    void deveListarTodasOrdenadasPorMaisRecentePrimeiro() throws Exception {
        mockMvc.perform(get("/tarefas"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=20")))
                .andExpect(header().string("Cache-Control", containsString("must-revalidate")))
                .andExpect(header().string("Cache-Control", containsString("private")))
                .andExpect(jsonPath("$[0].titulo").value("Tarefa recente"))
                .andExpect(jsonPath("$[1].titulo").value("Tarefa intermediaria"))
                .andExpect(jsonPath("$[2].titulo").value("Tarefa antiga"));
    }

    @Test
    void deveRetornarResumoPaginadoSemDescricao() throws Exception {
        mockMvc.perform(get("/tarefas/paginadas")
                        .param("page", "0")
                        .param("size", "2")
                        .param("status", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].titulo").value("Tarefa recente"))
                .andExpect(jsonPath("$.content[0].descricao").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
