package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Empregador;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface EmpregadorRepository extends CrudRepository<Empregador, Long> {

    List<Empregador> findByClienteIdUsuario(Long idCliente);

    long countByClienteIdUsuario(Long idCliente);
}
