package com.upeu.gestioninventario.estructuras.controller;

import com.upeu.gestioninventario.estructuras.dto.tipo.TipoEstructuraDTO;
import com.upeu.gestioninventario.estructuras.model.NivelEstructura;
import com.upeu.gestioninventario.estructuras.model.TipoEstructuraEntity;
import com.upeu.gestioninventario.estructuras.repository.TipoEstructuraRepository;
import com.upeu.gestioninventario.estructuras.mapper.TipoEstructuraMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TipoEstructuraController {

    private final TipoEstructuraRepository tipoEstructuraRepository;
    private final TipoEstructuraMapper tipoEstructuraMapper;

    @QueryMapping
    public List<TipoEstructuraDTO> tiposEstructura() {
        log.debug("GraphQL Query: tiposEstructura");
        List<TipoEstructuraEntity> tipos = tipoEstructuraRepository.findAll();
        return tipoEstructuraMapper.toDTOList(tipos);
    }

    @QueryMapping
    public TipoEstructuraDTO tipoEstructuraPorId(@Argument Long idTipo) {
        log.debug("GraphQL Query: tipoEstructuraPorId({})", idTipo);
        TipoEstructuraEntity tipo = tipoEstructuraRepository.findById(idTipo)
                .orElseThrow(() -> new RuntimeException("Tipo de estructura no encontrado"));
        return tipoEstructuraMapper.toDTO(tipo);
    }

    @QueryMapping
    public TipoEstructuraDTO tipoEstructuraPorCodigo(@Argument String codigo) {
        log.debug("GraphQL Query: tipoEstructuraPorCodigo({})", codigo);
        TipoEstructuraEntity tipo = tipoEstructuraRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Tipo de estructura no encontrado con código: " + codigo));
        return tipoEstructuraMapper.toDTO(tipo);
    }

    @QueryMapping
    public List<TipoEstructuraDTO> tiposEstructuraPorNivel(@Argument NivelEstructura nivel) {
        log.debug("GraphQL Query: tiposEstructuraPorNivel({})", nivel);
        List<TipoEstructuraEntity> tipos = tipoEstructuraRepository.findByNivelAplicableAndActivoTrue(nivel);
        return tipoEstructuraMapper.toDTOList(tipos);
    }
}
