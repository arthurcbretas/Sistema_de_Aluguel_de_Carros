package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Rendimento;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface RendimentoRepository extends CrudRepository<Rendimento, Long> {

    List<Rendimento> findByClienteIdUsuario(Long idCliente);

    long countByClienteIdUsuario(Long idCliente);
}
