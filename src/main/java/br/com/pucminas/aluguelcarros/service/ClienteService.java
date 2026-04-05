package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import br.com.pucminas.aluguelcarros.domain.model.Empregador;
import br.com.pucminas.aluguelcarros.domain.model.Rendimento;
import br.com.pucminas.aluguelcarros.dto.*;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.com.pucminas.aluguelcarros.exception.RegraDeNegocioException;
import br.com.pucminas.aluguelcarros.repository.ClienteRepository;
import br.com.pucminas.aluguelcarros.repository.EmpregadorRepository;
import br.com.pucminas.aluguelcarros.repository.RendimentoRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Singleton
public class ClienteService {

    private static final int LIMITE_RENDIMENTOS = 3;
    private static final int LIMITE_EMPREGADORES = 3;

    private final ClienteRepository clienteRepository;
    private final RendimentoRepository rendimentoRepository;
    private final EmpregadorRepository empregadorRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          RendimentoRepository rendimentoRepository,
                          EmpregadorRepository empregadorRepository) {
        this.clienteRepository = clienteRepository;
        this.rendimentoRepository = rendimentoRepository;
        this.empregadorRepository = empregadorRepository;
    }

    // ── CRUD Cliente ──────────────────────────────────────────────────────

    @Transactional
    public Cliente criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByLogin(dto.getLogin())) {
            throw new RegraDeNegocioException("Login já cadastrado: " + dto.getLogin());
        }
        if (clienteRepository.existsByCpf(dto.getCpf())) {
            throw new RegraDeNegocioException("CPF já cadastrado: " + dto.getCpf());
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setLogin(dto.getLogin());
        cliente.setSenha(BCrypt.hashpw(dto.getSenha(), BCrypt.gensalt(12))); 
        cliente.setEndereco(dto.getEndereco());
        cliente.setCpf(dto.getCpf());
        cliente.setRg(dto.getRg());
        cliente.setProfissao(dto.getProfissao());

        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(dto.getNome());
        cliente.setEndereco(dto.getEndereco());
        cliente.setProfissao(dto.getProfissao());
        return clienteRepository.update(cliente);
    }

    @Transactional
    public void deletar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    // ── Rendimentos ───────────────────────────────────────────────────────

    public List<Rendimento> listarRendimentos(Long idCliente) {
        buscarPorId(idCliente);
        return rendimentoRepository.findByClienteIdUsuario(idCliente);
    }

    @Transactional
    public Rendimento adicionarRendimento(Long idCliente, RendimentoDTO dto) {
        Cliente cliente = buscarPorId(idCliente);
        long total = rendimentoRepository.countByClienteIdUsuario(idCliente);
        if (total >= LIMITE_RENDIMENTOS) {
            throw new RegraDeNegocioException(
                    "Limite de " + LIMITE_RENDIMENTOS + " rendimentos atingido.");
        }
        Rendimento rendimento = new Rendimento();
        rendimento.setDescricao(dto.getDescricao());
        rendimento.setValorMensal(dto.getValorMensal());
        rendimento.setCliente(cliente);
        return rendimentoRepository.save(rendimento);
    }

    @Transactional
    public void removerRendimento(Long idCliente, Long idRendimento) {
        buscarPorId(idCliente);
        rendimentoRepository.findById(idRendimento)
                .filter(r -> r.getCliente().getIdUsuario().equals(idCliente))
                .ifPresentOrElse(
                        rendimentoRepository::delete,
                        () -> { throw new RecursoNaoEncontradoException("Rendimento", idRendimento); }
                );
    }

    // ── Empregadores ──────────────────────────────────────────────────────

    public List<Empregador> listarEmpregadores(Long idCliente) {
        buscarPorId(idCliente);
        return empregadorRepository.findByClienteIdUsuario(idCliente);
    }

    @Transactional
    public Empregador adicionarEmpregador(Long idCliente, EmpregadorDTO dto) {
        Cliente cliente = buscarPorId(idCliente);
        long total = empregadorRepository.countByClienteIdUsuario(idCliente);
        if (total >= LIMITE_EMPREGADORES) {
            throw new RegraDeNegocioException(
                    "Limite de " + LIMITE_EMPREGADORES + " empregadores atingido.");
        }
        Empregador empregador = new Empregador();
        empregador.setNome(dto.getNome());
        empregador.setCnpj(dto.getCnpj());
        empregador.setCargo(dto.getCargo());
        empregador.setCliente(cliente);
        return empregadorRepository.save(empregador);
    }
}
