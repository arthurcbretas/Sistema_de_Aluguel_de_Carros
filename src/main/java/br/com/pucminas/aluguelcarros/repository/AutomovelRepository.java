package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.model.Automovel;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutomovelRepository extends CrudRepository<Automovel, Long> {

    List<Automovel> findByDisponivelTrue();

    Optional<Automovel> findByPlaca(String placa);

    Optional<Automovel> findByMatricula(String matricula);
}
