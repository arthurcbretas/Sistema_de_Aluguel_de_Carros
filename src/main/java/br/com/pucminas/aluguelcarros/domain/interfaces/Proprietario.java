package br.com.pucminas.aluguelcarros.domain.interfaces;

/**
 * Contrato para entidades que podem ser proprietárias de automóveis.
 * Implementado por: Cliente, Empresa, Banco.
 */
public interface Proprietario {

    String getTipoProprietario();

    String getDocumento();
}
