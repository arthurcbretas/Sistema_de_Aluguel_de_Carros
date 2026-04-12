package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Empresa;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends CrudRepository<Empresa, Long> {
    Optional<Empresa> findByLogin(String login);
}
