package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    Optional<Cliente> findByLogin(String login);

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByLogin(String login);

    boolean existsByCpf(String cpf);
}
