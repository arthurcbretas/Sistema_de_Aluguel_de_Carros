package br.com.pucminas.aluguelcarros.service;

import br.com.pucminas.aluguelcarros.domain.model.Cliente;
import br.com.pucminas.aluguelcarros.dto.ClienteRequestDTO;
import br.com.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.com.pucminas.aluguelcarros.repository.ClienteRepository;
import br.com.pucminas.aluguelcarros.repository.EmpregadorRepository;
import br.com.pucminas.aluguelcarros.repository.RendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RendimentoRepository rendimentoRepository;

    @Mock
    private EmpregadorRepository empregadorRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO clienteDTO;

    @BeforeEach
    void setUp() {
        clienteDTO = new ClienteRequestDTO();
        clienteDTO.setNome("Arthur");
        clienteDTO.setLogin("arthur.login");
        clienteDTO.setSenha("senhaSecreta123");
        clienteDTO.setCpf("11122233344");
        clienteDTO.setRg("MG-12345");
        clienteDTO.setProfissao("Engenheiro");
        clienteDTO.setEndereco("Rua XYZ, 10");
    }

    @Test
    @DisplayName("Deve criar um cliente e fazer hash da senha com BCrypt")
    void deveCriarClienteComHash() {
        when(clienteRepository.existsByLogin(anyString())).thenReturn(false);
        when(clienteRepository.existsByCpf(anyString())).thenReturn(false);
        
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente saved = invocation.getArgument(0);
            saved.setIdUsuario(1L);
            return saved;
        });

        Cliente criado = clienteService.criar(clienteDTO);

        assertNotNull(criado);
        assertNotEquals("senhaSecreta123", criado.getSenha());
        assertTrue(BCrypt.checkpw("senhaSecreta123", criado.getSenha()), "A senha persistida deve bater com a original avaliada via BCrypt");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve deletar o cliente com sucesso se ele existir")
    void deveDeletarClienteExistente() {
        Cliente mockCliente = new Cliente();
        mockCliente.setIdUsuario(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(mockCliente));
        doNothing().when(clienteRepository).delete(mockCliente);

        assertDoesNotThrow(() -> clienteService.deletar(1L));
        verify(clienteRepository, times(1)).delete(mockCliente);
    }

    @Test
    @DisplayName("Deve falhar ao tentar deletar um cliente inexistente")
    void naoDeveDeletarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.deletar(99L));
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }
}
