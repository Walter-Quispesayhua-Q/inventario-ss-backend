-- Indices para optimizacion de consultas

-- Indices para personas
CREATE INDEX idx_personas_codigo ON personas.personas(codigo);
CREATE INDEX idx_personas_email ON personas.personas(email);
CREATE INDEX idx_personas_identificacion ON personas.personas(identificacion);
CREATE INDEX idx_personas_nombre_apellido ON personas.personas(nombre, apellido);

-- Indices para usuarios
CREATE INDEX idx_usuarios_id_persona ON seguridad.usuarios(id_persona);
CREATE INDEX idx_usuarios_codigo ON seguridad.usuarios(codigo_usuario);
CREATE INDEX idx_usuarios_email ON seguridad.usuarios(email);

-- Indices para ubicaciones
CREATE INDEX idx_ubicaciones_edificio_viejo ON ubicaciones.ubicaciones(edificio);
CREATE INDEX idx_ubicaciones_nombre ON ubicaciones.ubicaciones(nombre_ubicacion);
CREATE INDEX idx_ubicaciones_fecha_eliminacion ON ubicaciones.ubicaciones(fecha_eliminacion);
CREATE INDEX idx_ubicaciones_edificio ON ubicaciones.ubicaciones(id_edificio);
CREATE INDEX idx_ubicaciones_estacion ON ubicaciones.ubicaciones(id_estacion);
CREATE INDEX idx_departamentos_nombre ON ubicaciones.departamentos(nombre_departamento);

-- Indices para categorias
CREATE INDEX idx_categoria_nombre ON inventario.categorias(nombre_categoria);
CREATE INDEX idx_categoria_estado ON inventario.categorias(estado);
CREATE INDEX idx_categoria_fecha_eliminacion ON inventario.categorias(fecha_eliminacion);
CREATE INDEX idx_categoria_estado_activo ON inventario.categorias(estado, fecha_eliminacion);

-- Indices para bienes (optimizados)
CREATE UNIQUE INDEX idx_bienes_caf_unique ON inventario.bienes (caf)
    WHERE caf IS NOT NULL
      AND upper(caf) NOT IN (
        'ARRENDADO', 'PRESTADO', 'DONADO', 'COMODATO', 'ALQUILADO',
        '-', '--', 'N/A', 'NA', 'NO APLICA', 'SIN CAF',
        'NO EXISTE', 'NO ENCONTRADO', 'PENDIENTE', 'DESCONOCIDO',
        'POR ASIGNAR', 'SIN ASIGNAR', 'TEMPORAL'
      )
      AND fecha_eliminacion IS NULL;

CREATE UNIQUE INDEX idx_bienes_numero_serie_unique ON inventario.bienes(numero_serie) WHERE numero_serie IS NOT NULL;
CREATE INDEX idx_bienes_nombre_bien ON inventario.bienes(nombre_bien);
CREATE INDEX idx_bienes_caf ON inventario.bienes(caf);
CREATE INDEX idx_bienes_numero_serie ON inventario.bienes(numero_serie);
CREATE INDEX idx_bienes_id_categoria ON inventario.bienes(id_categoria);
CREATE INDEX idx_bienes_id_ubicacion_actual ON inventario.bienes(id_ubicacion_actual);
CREATE INDEX idx_bienes_id_responsable_actual ON inventario.bienes(id_responsable_actual);
CREATE INDEX idx_bienes_id_departamento ON inventario.bienes(id_departamento);
CREATE INDEX idx_bienes_estado_operacional ON inventario.bienes(estado_operacional);
CREATE INDEX idx_bienes_estado_fisico ON inventario.bienes(estado_fisico);
CREATE INDEX idx_bienes_fecha_eliminacion ON inventario.bienes(fecha_eliminacion);
CREATE INDEX idx_bienes_fecha_creacion ON inventario.bienes(fecha_creacion DESC);
CREATE INDEX idx_bienes_categoria_estado ON inventario.bienes(id_categoria, estado_operacional) WHERE fecha_eliminacion IS NULL;
CREATE INDEX idx_bienes_ubicacion_categoria ON inventario.bienes(id_ubicacion_actual, id_categoria) WHERE fecha_eliminacion IS NULL;

-- Indices para estructuras
CREATE INDEX idx_edificios_codigo ON estructuras.edificios(codigo);
CREATE INDEX idx_edificios_activos ON estructuras.edificios(fecha_eliminacion) WHERE fecha_eliminacion IS NULL;
CREATE INDEX idx_edificios_responsable ON estructuras.edificios(id_responsable_mantenimiento);
CREATE INDEX idx_pisos_edificio ON estructuras.pisos(id_edificio);
CREATE INDEX idx_pisos_codigo ON estructuras.pisos(codigo);
CREATE INDEX idx_pisos_numero ON estructuras.pisos(numero_piso);
CREATE INDEX idx_pisos_activos ON estructuras.pisos(id_edificio, fecha_eliminacion) WHERE fecha_eliminacion IS NULL;
CREATE INDEX idx_ambientes_piso ON estructuras.ambientes(id_piso);
CREATE INDEX idx_ambientes_codigo ON estructuras.ambientes(codigo);
CREATE INDEX idx_ambientes_departamento ON estructuras.ambientes(id_departamento_responsable);
CREATE INDEX idx_ambientes_activos ON estructuras.ambientes(id_piso, fecha_eliminacion) WHERE fecha_eliminacion IS NULL;
CREATE INDEX idx_estaciones_ambiente ON estructuras.estaciones(id_ambiente);
CREATE INDEX idx_estaciones_codigo ON estructuras.estaciones(codigo);
CREATE INDEX idx_estaciones_responsable ON estructuras.estaciones(id_responsable);
CREATE INDEX idx_estaciones_activas ON estructuras.estaciones(id_ambiente, fecha_eliminacion) WHERE fecha_eliminacion IS NULL;
CREATE INDEX idx_componente_estacion ON estructuras.componentes_estaciones(id_estacion);
CREATE INDEX idx_componente_tipo ON estructuras.componentes_estaciones(tipo);
CREATE INDEX idx_bienes_estaciones_bien ON estructuras.bienes_estaciones(id_bien);
CREATE INDEX idx_bienes_estaciones_estacion ON estructuras.bienes_estaciones(id_estacion);
CREATE INDEX idx_bienes_estaciones_activas ON estructuras.bienes_estaciones(fecha_desasignacion) WHERE fecha_desasignacion IS NULL;
CREATE INDEX idx_bien_componente ON estructuras.bienes_estaciones(id_componente);
CREATE UNIQUE INDEX uk_bien_estacion_activo ON estructuras.bienes_estaciones(id_bien) WHERE fecha_desasignacion IS NULL;

-- Indices para historial y atributos
CREATE INDEX idx_historial_ubicaciones_id_bien ON inventario.historial_ubicaciones(id_bien);
CREATE INDEX idx_historial_ubicaciones_fecha_entrada ON inventario.historial_ubicaciones(fecha_entrada);
CREATE INDEX idx_bien_atributos_valores_id_bien ON inventario.bien_atributos_valores(id_bien);
CREATE INDEX idx_bien_atributos_valores_id_atributo ON inventario.bien_atributos_valores(id_atributo);

-- Indices para auditoria
CREATE INDEX idx_registro_fecha ON auditoria.registros_actividad(fecha_operacion);
CREATE INDEX idx_registro_usuario ON auditoria.registros_actividad(id_usuario);
CREATE INDEX idx_registro_tipo_op ON auditoria.registros_actividad(tipo_operacion);
CREATE INDEX idx_registro_entidad ON auditoria.registros_actividad(entidad_afectada);

-- Indices para configuracion
CREATE INDEX idx_seed_state_nombre ON configuracion.seed_file_state(nombre_archivo);
CREATE INDEX idx_seed_state_tipo ON configuracion.seed_file_state(tipo_seed);
