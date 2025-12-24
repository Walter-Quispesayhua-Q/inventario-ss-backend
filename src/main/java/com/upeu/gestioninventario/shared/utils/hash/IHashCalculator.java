package com.upeu.gestioninventario.shared.utils.hash;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Contrato para servicios de cálculo de hash criptográfico.
 * <p>
 * Permite calcular hashes SHA-256 de archivos y recursos para
 * detección de cambios y verificación de integridad.
 */
public interface IHashCalculator {

    /**
     * Calcula el hash SHA-256 de un InputStream.
     * 
     * @param inputStream Stream de entrada a procesar
     * @return Hash en formato hexadecimal
     */
    String calcularHash(InputStream inputStream);

    /**
     * Calcula el hash SHA-256 de un Resource de Spring.
     * 
     * @param resource Recurso a procesar
     * @return Hash en formato hexadecimal
     */
    String calcularHash(Resource resource);
}
