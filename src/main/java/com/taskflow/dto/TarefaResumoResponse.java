package com.taskflow.dto;

import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;

import java.time.LocalDateTime;

public record TarefaResumoResponse(
        Long id,
        String titulo,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        LocalDateTime dataCriacao
) {
}
