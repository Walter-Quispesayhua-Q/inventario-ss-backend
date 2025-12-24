package com.upeu.gestioninventario.importacion.service.persistencia.impl;

import com.upeu.gestioninventario.categorias.dto.CategoriaCreacionDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaDTO;
import com.upeu.gestioninventario.categorias.model.Categoria;
import com.upeu.gestioninventario.categorias.repository.CategoriaRepository;
import com.upeu.gestioninventario.categorias.service.ICategoriaService;
import com.upeu.gestioninventario.importacion.service.persistencia.IResolverCategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolverCategoriaServiceImpl implements IResolverCategoriaService {

    private final ICategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Categoria> resolverPorNombre(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            log.error("Se intento resolver con un nombre vacio");
            return Optional.empty();
        }

        String nombreNormalizado = nombreCategoria.trim();
        log.debug("Resolviendo categoria por nombre '{}'", nombreNormalizado);

        Optional<Categoria> existente = categoriaRepository
                .findByNombreCategoriaIgnoreCaseAndFechaEliminacionIsNull(nombreNormalizado);

        if (existente.isPresent()) {
            return activarCategoriaParaImportacion(existente.get());
        }

        return crearCategoriaParaImportacion(nombreNormalizado);
    }

    private Optional<Categoria> activarCategoriaParaImportacion(Categoria categoria) {
        if (!Boolean.TRUE.equals(categoria.getVisible())) {
            categoria.setVisible(true);
            categoriaRepository.save(categoria);
            log.info("Categoria '{}' activada para importacion", categoria.getNombreCategoria());
        }
        return Optional.of(categoria);
    }

    private Optional<Categoria> crearCategoriaParaImportacion(String nombreCategoria) {
        log.info("Categoria '{}' no encontrada, creandola", nombreCategoria);
        
        CategoriaCreacionDTO nuevaCategoria = new CategoriaCreacionDTO(
                nombreCategoria,
                "Categoria creada automaticamente desde importacion",
                "pi pi-box",
                "#CCCCCC",
                true
        );
        
        var resultado = categoriaService.crearCategoria(nuevaCategoria);
        if (!resultado.getExito() || resultado.getData() == null) {
            log.error("Error al crear categoria para importacion: {}", resultado.getMensaje());
            return Optional.empty();
        }
        
        CategoriaDTO dtoCreado = resultado.getData();
        return categoriaRepository.findById(dtoCreado.id());
    }
}