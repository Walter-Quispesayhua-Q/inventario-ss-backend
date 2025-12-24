package com.upeu.gestioninventario.shared.utils.hash;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementación de calculadora de hash usando SHA-256.
 * <p>
 * Utiliza el algoritmo SHA-256 para generar hashes criptográficos
 * de archivos y recursos, útil para detectar cambios en archivos seed
 * y validación de integridad.
 */
@Service
@Slf4j
public class HashCalculatorServiceImpl implements IHashCalculator {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    @Override
    public String calcularHash(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("InputStream nulo, retornando hash vacío");
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return bytesToHex(digest.digest());

        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo {} no disponible", ALGORITHM, e);
            throw new RuntimeException("Algoritmo de hash no disponible: " + ALGORITHM, e);
        } catch (IOException e) {
            log.error("Error leyendo InputStream para calcular hash", e);
            throw new RuntimeException("Error al calcular hash del archivo", e);
        }
    }

    @Override
    public String calcularHash(Resource resource) {
        if (resource == null || !resource.exists()) {
            log.warn("Resource nulo o inexistente, retornando hash vacío");
            return "";
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return calcularHash(inputStream);
        } catch (IOException e) {
            log.error("Error abriendo Resource para calcular hash: {}", resource.getDescription(), e);
            throw new RuntimeException("Error al calcular hash del recurso: " + resource.getDescription(), e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
