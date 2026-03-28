package br.com.pucminas.aluguelcarros.repository;

import br.com.pucminas.aluguelcarros.domain.enums.StatusPedido;
import br.com.pucminas.aluguelcarros.domain.model.Pedido;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface PedidoRepository extends CrudRepository<Pedido, Long> {

    List<Pedido> findByClienteIdUsuario(Long idCliente);

    List<Pedido> findByStatus(StatusPedido status);
}
