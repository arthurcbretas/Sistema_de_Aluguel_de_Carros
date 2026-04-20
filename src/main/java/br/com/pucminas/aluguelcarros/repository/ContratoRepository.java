package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Contrato;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends CrudRepository<Contrato, Long> {

    Optional<Contrato> findByPedidoIdPedido(Long idPedido);

    @Query("SELECT c FROM Contrato c WHERE c.contratoCredito IS NULL AND c.necessitaCredito = true")
    List<Contrato> findSemCredito();
}
