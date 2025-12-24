package com.upeu.gestioninventario.categorias.controller;

import com.upeu.gestioninventario.categorias.dto.CategoriaActualizacionDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaCreacionDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriaSimpleDTO;
import com.upeu.gestioninventario.categorias.dto.CategoriasPaginadas;
import com.upeu.gestioninventario.categorias.dto.PlantillaCategoriaDTO;
import com.upeu.gestioninventario.categorias.service.ICategoriaService;
import com.upeu.gestioninventario.categorias.service.IFormularioDinamicoService;
import com.upeu.gestioninventario.inventario.dto.AtributoFormularioDTO;
import com.upeu.gestioninventario.shared.dto.response.OperacionResultadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CategoriaController {

    private final ICategoriaService categoriaService;
    private final IFormularioDinamicoService formularioDinamicoService;

    @MutationMapping(value = "crearCategoria")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<CategoriaDTO> crearCategoria(@Argument("categoria") CategoriaCreacionDTO formData) {
        log.info("Recibida solicitud para crear categoría: {}", formData.nombreCategoria());
        return categoriaService.crearCategoria(formData);
    }

    @MutationMapping(value = "actualizarCategoria")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<CategoriaDTO> actualizarCategoria(
            @Argument("id") Long id,
            @Argument("categoria") CategoriaActualizacionDTO updateData
    ) {
        log.info("Recibida solicitud para actualizar categoría ID: {}", id);
        return categoriaService.actualizarCategoria(id, updateData);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<Boolean> eliminarCategoria(@Argument("id") Long id) {
        log.info("Recibida solicitud para eliminar categoría ID: {}", id);
        return categoriaService.eliminarCategoria(id);
    }

    @QueryMapping
    public CategoriasPaginadas categorias(
            @Argument Integer page,
            @Argument Integer limit,
            @Argument Boolean estado,
            @Argument String ordenarPor,
            @Argument String orden,
            @Argument String buscar
    ) {
        log.info("Consulta de categorías paginadas - página: {}, límite: {}", page, limit);
        return categoriaService.obtenerCategoriasPaginadas(page, limit, estado, ordenarPor, orden, buscar);
    }

    @QueryMapping
    public List<PlantillaCategoriaDTO> sugerirPlantillasParaCategoria(@Argument("nombreCategoria") String nombreCategoria) {
        return categoriaService.sugerirPlantillas(nombreCategoria);
    }

    @MutationMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER_INTERNO')")
    public OperacionResultadoDTO<CategoriaDTO> aplicarPlantillaACategoria(
            @Argument("categoriaId") Long categoriaId,
            @Argument("plantillaId") Long plantillaId
    ) {
        log.info("Recibida solicitud para aplicar plantilla {} a categoría {}", plantillaId, categoriaId);
        return categoriaService.aplicarPlantilla(categoriaId, plantillaId);
    }

    @QueryMapping
    public List<CategoriaSimpleDTO> categoriasSimplesDisponibles() {
        return categoriaService.obtenerCategoriasSimples();
    }

    @QueryMapping
    public List<AtributoFormularioDTO> atributosParaFormularioPorCategoria(
            @Argument Long categoriaId, @Argument String buscarNombrePlantilla
    ) {
        return formularioDinamicoService.obtenerAtributosParaCategoria(categoriaId, buscarNombrePlantilla);
    }
}