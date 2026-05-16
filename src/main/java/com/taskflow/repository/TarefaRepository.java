package com.taskflow.repository;

import com.taskflow.dto.TarefaResumoResponse;
import com.taskflow.entity.PrioridadeTarefa;
import com.taskflow.entity.StatusTarefa;
import com.taskflow.entity.Tarefa;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "50")
    })
    List<Tarefa> findAllByOrderByDataCriacaoDescIdDesc();

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "50")
    })
    List<Tarefa> findByStatusOrderByDataCriacaoDescIdDesc(StatusTarefa status);

    @Query(
            value = """
                    select new com.taskflow.dto.TarefaResumoResponse(
                        t.id,
                        t.titulo,
                        t.status,
                        t.prioridade,
                        t.dataCriacao
                    )
                    from Tarefa t
                    where (:status is null or t.status = :status)
                      and (:prioridade is null or t.prioridade = :prioridade)
                    """,
            countQuery = """
                    select count(t)
                    from Tarefa t
                    where (:status is null or t.status = :status)
                      and (:prioridade is null or t.prioridade = :prioridade)
                    """
    )
    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"),
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "50")
    })
    Page<TarefaResumoResponse> buscarResumos(
            @Param("status") StatusTarefa status,
            @Param("prioridade") PrioridadeTarefa prioridade,
            Pageable pageable
    );
}
