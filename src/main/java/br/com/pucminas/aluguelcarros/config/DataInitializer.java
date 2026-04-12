package br.com.pucminas.aluguelcarros.config;

import br.com.pucminas.aluguelcarros.domain.model.Automovel;
import br.com.pucminas.aluguelcarros.domain.model.Empresa;
import br.com.pucminas.aluguelcarros.domain.model.Agente;
import br.com.pucminas.aluguelcarros.repository.AutomovelRepository;
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

    public DataInitializer(EmpresaRepository empresaRepository, AutomovelRepository automovelRepository) {
        this.empresaRepository = empresaRepository;
        this.automovelRepository = automovelRepository;
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

            Automovel a1 = new Automovel(null, "MERC-001", "ABC-1234", "Mercedes-Benz", "C-Class", 2023, true, null, null, mockEmpresa, null);
            a1.setPrecoDiaria(new BigDecimal("350.00"));

            Automovel a2 = new Automovel(null, "BMW-002", "XYZ-9876", "BMW", "Serie 3", 2024, true, null, null, mockEmpresa, null);
            a2.setPrecoDiaria(new BigDecimal("420.00"));

            Automovel a3 = new Automovel(null, "AUDI-003", "QWE-4567", "Audi", "A5 Sportback", 2023, true, null, null, mockEmpresa, null);
            a3.setPrecoDiaria(new BigDecimal("390.00"));
            
            automovelRepository.saveAll(List.of(a1, a2, a3));
        }
    }
}
