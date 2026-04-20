package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Banco;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface BancoRepository extends CrudRepository<Banco, Long> {
    Optional<Banco> findByLogin(String login);
}
