package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.ContratoCredito;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface ContratoCreditoRepository extends CrudRepository<ContratoCredito, Long> {

    Optional<ContratoCredito> findByContratoIdContrato(Long idContrato);
}
