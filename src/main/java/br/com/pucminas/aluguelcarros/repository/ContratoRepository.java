package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Contrato;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface ContratoRepository extends CrudRepository<Contrato, Long> {

    Optional<Contrato> findByPedidoIdPedido(Long idPedido);
}
