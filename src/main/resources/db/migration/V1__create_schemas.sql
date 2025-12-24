-- Eliminar schemas existentes (solo para desarrollo/reset)

-- DROP SCHEMA IF EXISTS inventario CASCADE;
-- DROP SCHEMA IF EXISTS seguridad CASCADE;
-- DROP SCHEMA IF EXISTS configuracion CASCADE;
-- DROP SCHEMA IF EXISTS personas CASCADE;
-- DROP SCHEMA IF EXISTS ubicaciones CASCADE;
-- DROP SCHEMA IF EXISTS estructuras CASCADE;
-- DROP SCHEMA IF EXISTS auditoria CASCADE;

-- Crear schemas
CREATE SCHEMA IF NOT EXISTS inventario;
CREATE SCHEMA IF NOT EXISTS seguridad;
CREATE SCHEMA IF NOT EXISTS configuracion;
CREATE SCHEMA IF NOT EXISTS personas;
CREATE SCHEMA IF NOT EXISTS ubicaciones;
CREATE SCHEMA IF NOT EXISTS estructuras;
CREATE SCHEMA IF NOT EXISTS auditoria;
