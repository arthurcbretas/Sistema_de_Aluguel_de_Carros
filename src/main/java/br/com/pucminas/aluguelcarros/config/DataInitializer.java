package br.com.pucminas.aluguelcarros.config;

import br.com.pucminas.aluguelcarros.domain.model.Automovel;
import br.com.pucminas.aluguelcarros.domain.model.Banco;
import br.com.pucminas.aluguelcarros.domain.model.Empresa;
import br.com.pucminas.aluguelcarros.domain.model.Agente;
import br.com.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.com.pucminas.aluguelcarros.repository.BancoRepository;
import br.com.pucminas.aluguelcarros.repository.EmpresaRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class DataInitializer implements ApplicationEventListener<StartupEvent> {

    private final EmpresaRepository empresaRepository;
    private final AutomovelRepository automovelRepository;
    private final BancoRepository bancoRepository;

    public DataInitializer(EmpresaRepository empresaRepository, AutomovelRepository automovelRepository, BancoRepository bancoRepository) {
        this.empresaRepository = empresaRepository;
        this.automovelRepository = automovelRepository;
        this.bancoRepository = bancoRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(StartupEvent event) {
        if (empresaRepository.count() == 0) {
            Empresa mockEmpresa = new Empresa();
            mockEmpresa.setNome("DriveLux Corporativa");
            mockEmpresa.setLogin("drivelux@admin.com");
            mockEmpresa.setSenha("123456"); // Fake hash ignore 
            mockEmpresa.setCnpj("00.000.000/0001-00");
            mockEmpresa.setRazaoSocial("DriveLux Locações Comerciais LTDA");
            
            empresaRepository.save(mockEmpresa);

            Automovel a1 = new Automovel();
            a1.setMatricula("MERC-001");
            a1.setPlaca("ABC-1234");
            a1.setMarca("Mercedes-Benz");
            a1.setModelo("C-Class");
            a1.setAno(2023);
            a1.setDisponivel(true);
            a1.setPrecoDiaria(new BigDecimal("1350.00"));
            a1.setImagemUrl("https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=600&h=400&fit=crop");
            a1.setEmpresaProprietaria(mockEmpresa);

            Automovel a2 = new Automovel();
            a2.setMatricula("BMW-002");
            a2.setPlaca("XYZ-9876");
            a2.setMarca("BMW");
            a2.setModelo("Serie 3");
            a2.setAno(2024);
            a2.setDisponivel(true);
            a2.setPrecoDiaria(new BigDecimal("1420.00"));
            a2.setImagemUrl("https://images.unsplash.com/photo-1555215695-3004980ad54e?w=600&h=400&fit=crop");
            a2.setEmpresaProprietaria(mockEmpresa);

            Automovel a3 = new Automovel();
            a3.setMatricula("AUDI-003");
            a3.setPlaca("QWE-4567");
            a3.setMarca("Audi");
            a3.setModelo("A5 Sportback");
            a3.setAno(2023);
            a3.setDisponivel(true);
            a3.setPrecoDiaria(new BigDecimal("1390.00"));
            a3.setImagemUrl("https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=600&h=400&fit=crop");
            a3.setEmpresaProprietaria(mockEmpresa);
            
            automovelRepository.saveAll(List.of(a1, a2, a3));

            // ── Seed Banco ────────────────────────────────────────────────
            Banco mockBanco = new Banco();
            mockBanco.setNome("Banco DriveLux");
            mockBanco.setLogin("banco@drivelux.com");
            mockBanco.setSenha("123456");
            mockBanco.setCnpj("11.111.111/0001-11");
            mockBanco.setNomeBanco("Banco DriveLux Finance");
            mockBanco.setCodigoBancario("999");
            bancoRepository.save(mockBanco);
        } else {
            // Atualiza os preços antigos no banco de dados existente (produção)
            automovelRepository.findAll().forEach(a -> {
                if (a.getPrecoDiaria().compareTo(new BigDecimal("1000")) < 0) {
                    if (a.getModelo().equals("C-Class")) a.setPrecoDiaria(new BigDecimal("1350.00"));
                    else if (a.getModelo().equals("Serie 3")) a.setPrecoDiaria(new BigDecimal("1420.00"));
                    else if (a.getModelo().equals("A5 Sportback")) a.setPrecoDiaria(new BigDecimal("1390.00"));
                    automovelRepository.update(a);
                }
            });
        }
    }
}
