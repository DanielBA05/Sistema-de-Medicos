--
-- PostgreSQL database dump
--

\restrict t1401fcLg2Usi2cnc0nbGmxwo6vcfujaZtBubBVdNGdv7flEWsHunX9v96E5dhA

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2025-10-08 14:35:07

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 16525)
-- Name: clinica; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA clinica;


ALTER SCHEMA clinica OWNER TO postgres;

--
-- TOC entry 235 (class 1255 OID 16616)
-- Name: agendar_cita(integer, integer, timestamp without time zone, character varying); Type: PROCEDURE; Schema: clinica; Owner: postgres
--

CREATE PROCEDURE clinica.agendar_cita(IN p_id_paciente integer, IN p_id_doctor integer, IN p_fecha_hora timestamp without time zone, IN p_motivo character varying)
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_count INT;
BEGIN
  -- verificar paciente
  SELECT COUNT(*) INTO v_count FROM clinica.paciente WHERE id_paciente = p_id_paciente;
  IF v_count = 0 THEN
    RAISE EXCEPTION 'Paciente no existe';
  END IF;

  -- verificar doctor
  SELECT COUNT(*) INTO v_count FROM clinica.doctor WHERE id_doctor = p_id_doctor;
  IF v_count = 0 THEN
    RAISE EXCEPTION 'Doctor no existe';
  END IF;

  IF p_fecha_hora <= now() THEN
    RAISE EXCEPTION 'La cita debe ser en fecha futura';
  END IF;

  -- verificar disponibilidad +/- 30 minutos
  SELECT COUNT(*) INTO v_count
  FROM clinica.cita
  WHERE id_doctor = p_id_doctor
    AND fecha_hora BETWEEN p_fecha_hora - INTERVAL '30 minutes' AND p_fecha_hora + INTERVAL '30 minutes';

  IF v_count > 0 THEN
    RAISE EXCEPTION 'Doctor no disponible en ese horario';
  END IF;

  INSERT INTO clinica.cita (fecha_hora, id_paciente, id_doctor, motivo)
  VALUES (p_fecha_hora, p_id_paciente, p_id_doctor, p_motivo);
END;
$$;


ALTER PROCEDURE clinica.agendar_cita(IN p_id_paciente integer, IN p_id_doctor integer, IN p_fecha_hora timestamp without time zone, IN p_motivo character varying) OWNER TO postgres;

--
-- TOC entry 237 (class 1255 OID 16628)
-- Name: audit_trigger(); Type: FUNCTION; Schema: clinica; Owner: postgres
--

CREATE FUNCTION clinica.audit_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  pk_val TEXT;
  old_j JSONB;
  new_j JSONB;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    old_j := to_jsonb(OLD);
    pk_val := (old_j ->> TG_ARGV[0]);
    INSERT INTO clinica.audit_log (operacion, tabla_nombre, pk_valor, usuario_nombre, old_data, new_data)
    VALUES (TG_OP, TG_TABLE_NAME, pk_val, current_user, old_j, NULL);
    RETURN OLD;
  ELSIF (TG_OP = 'INSERT') THEN
    new_j := to_jsonb(NEW);
    pk_val := (new_j ->> TG_ARGV[0]);
    INSERT INTO clinica.audit_log (operacion, tabla_nombre, pk_valor, usuario_nombre, old_data, new_data)
    VALUES (TG_OP, TG_TABLE_NAME, pk_val, current_user, NULL, new_j);
    RETURN NEW;
  ELSE -- UPDATE
    old_j := to_jsonb(OLD);
    new_j := to_jsonb(NEW);
    pk_val := COALESCE(new_j ->> TG_ARGV[0], old_j ->> TG_ARGV[0]);
    INSERT INTO clinica.audit_log (operacion, tabla_nombre, pk_valor, usuario_nombre, old_data, new_data)
    VALUES (TG_OP, TG_TABLE_NAME, pk_val, current_user, old_j, new_j);
    RETURN NEW;
  END IF;
END;
$$;


ALTER FUNCTION clinica.audit_trigger() OWNER TO postgres;

--
-- TOC entry 234 (class 1255 OID 16615)
-- Name: calcular_edad(date); Type: FUNCTION; Schema: clinica; Owner: postgres
--

CREATE FUNCTION clinica.calcular_edad(fecha_nac date) RETURNS integer
    LANGUAGE sql
    AS $$
  SELECT EXTRACT(YEAR FROM AGE(CURRENT_DATE, fecha_nac))::INT;
$$;


ALTER FUNCTION clinica.calcular_edad(fecha_nac date) OWNER TO postgres;

--
-- TOC entry 236 (class 1255 OID 16617)
-- Name: importar_no_normalizados(text); Type: PROCEDURE; Schema: clinica; Owner: postgres
--

CREATE PROCEDURE clinica.importar_no_normalizados(IN p_src_table text)
    LANGUAGE plpgsql
    AS $$
DECLARE
  rec RECORD;
BEGIN
  FOR rec IN EXECUTE format('SELECT * FROM %I', p_src_table)
  LOOP
    -- ejemplo suponiendo campos: nombre, apellido, telefono
    INSERT INTO clinica.paciente (nombre, apellido, fecha_nacimiento, telefono)
    VALUES (rec.nombre, rec.apellido, COALESCE(rec.fecha_nacimiento, '1900-01-01'), rec.telefono);
  END LOOP;
END;
$$;


ALTER PROCEDURE clinica.importar_no_normalizados(IN p_src_table text) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 233 (class 1259 OID 16619)
-- Name: audit_log; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.audit_log (
    id_log integer NOT NULL,
    operacion character varying(10),
    tabla_nombre character varying(100),
    pk_valor text,
    usuario_nombre text,
    old_data jsonb,
    new_data jsonb,
    fecha timestamp without time zone DEFAULT now()
);


ALTER TABLE clinica.audit_log OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 16618)
-- Name: audit_log_id_log_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.audit_log_id_log_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.audit_log_id_log_seq OWNER TO postgres;

--
-- TOC entry 4988 (class 0 OID 0)
-- Dependencies: 232
-- Name: audit_log_id_log_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.audit_log_id_log_seq OWNED BY clinica.audit_log.id_log;


--
-- TOC entry 227 (class 1259 OID 16568)
-- Name: cita; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.cita (
    id_cita bigint NOT NULL,
    fecha_hora timestamp without time zone NOT NULL,
    id_paciente bigint NOT NULL,
    id_doctor bigint NOT NULL,
    motivo character varying(255)
);


ALTER TABLE clinica.cita OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16567)
-- Name: cita_id_cita_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.cita_id_cita_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.cita_id_cita_seq OWNER TO postgres;

--
-- TOC entry 4989 (class 0 OID 0)
-- Dependencies: 226
-- Name: cita_id_cita_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.cita_id_cita_seq OWNED BY clinica.cita.id_cita;


--
-- TOC entry 225 (class 1259 OID 16551)
-- Name: doc_especialidad; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.doc_especialidad (
    id_doc_especialidad integer NOT NULL,
    id_doctor bigint NOT NULL,
    id_especialidad bigint NOT NULL
);


ALTER TABLE clinica.doc_especialidad OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16550)
-- Name: doc_especialidad_id_doc_especialidad_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.doc_especialidad_id_doc_especialidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.doc_especialidad_id_doc_especialidad_seq OWNER TO postgres;

--
-- TOC entry 4990 (class 0 OID 0)
-- Dependencies: 224
-- Name: doc_especialidad_id_doc_especialidad_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.doc_especialidad_id_doc_especialidad_seq OWNED BY clinica.doc_especialidad.id_doc_especialidad;


--
-- TOC entry 221 (class 1259 OID 16534)
-- Name: doctor; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.doctor (
    id_doctor bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    direccion character varying(150),
    telefono character varying(50)
);


ALTER TABLE clinica.doctor OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16533)
-- Name: doctor_id_doctor_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.doctor_id_doctor_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.doctor_id_doctor_seq OWNER TO postgres;

--
-- TOC entry 4991 (class 0 OID 0)
-- Dependencies: 220
-- Name: doctor_id_doctor_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.doctor_id_doctor_seq OWNED BY clinica.doctor.id_doctor;


--
-- TOC entry 219 (class 1259 OID 16527)
-- Name: especialidad; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.especialidad (
    id_especialidad bigint NOT NULL,
    nom_especialidad character varying(100) NOT NULL
);


ALTER TABLE clinica.especialidad OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16526)
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.especialidad_id_especialidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.especialidad_id_especialidad_seq OWNER TO postgres;

--
-- TOC entry 4992 (class 0 OID 0)
-- Dependencies: 218
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.especialidad_id_especialidad_seq OWNED BY clinica.especialidad.id_especialidad;


--
-- TOC entry 229 (class 1259 OID 16585)
-- Name: historial_medico; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.historial_medico (
    id_historial bigint NOT NULL,
    id_paciente bigint NOT NULL,
    id_cita bigint,
    fecha_consulta date,
    diagnostico text,
    tratamiento text
);


ALTER TABLE clinica.historial_medico OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16584)
-- Name: historial_medico_id_historial_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.historial_medico_id_historial_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.historial_medico_id_historial_seq OWNER TO postgres;

--
-- TOC entry 4993 (class 0 OID 0)
-- Dependencies: 228
-- Name: historial_medico_id_historial_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.historial_medico_id_historial_seq OWNED BY clinica.historial_medico.id_historial;


--
-- TOC entry 223 (class 1259 OID 16541)
-- Name: paciente; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.paciente (
    id_paciente bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    fecha_nacimiento date NOT NULL,
    sexo character varying(1),
    direccion character varying(255),
    telefono character varying(255) NOT NULL,
    correo character varying(255),
    CONSTRAINT paciente_sexo_check CHECK (((sexo)::bpchar = ANY (ARRAY['M'::bpchar, 'F'::bpchar, 'O'::bpchar])))
);


ALTER TABLE clinica.paciente OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16540)
-- Name: paciente_id_paciente_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.paciente_id_paciente_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.paciente_id_paciente_seq OWNER TO postgres;

--
-- TOC entry 4994 (class 0 OID 0)
-- Dependencies: 222
-- Name: paciente_id_paciente_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.paciente_id_paciente_seq OWNED BY clinica.paciente.id_paciente;


--
-- TOC entry 231 (class 1259 OID 16604)
-- Name: receta; Type: TABLE; Schema: clinica; Owner: postgres
--

CREATE TABLE clinica.receta (
    id_receta bigint NOT NULL,
    medicamento character varying(200) NOT NULL,
    dosis character varying(100) NOT NULL,
    frecuencia character varying(100),
    duracion integer,
    id_historial bigint NOT NULL
);


ALTER TABLE clinica.receta OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16603)
-- Name: receta_id_receta_seq; Type: SEQUENCE; Schema: clinica; Owner: postgres
--

CREATE SEQUENCE clinica.receta_id_receta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE clinica.receta_id_receta_seq OWNER TO postgres;

--
-- TOC entry 4995 (class 0 OID 0)
-- Dependencies: 230
-- Name: receta_id_receta_seq; Type: SEQUENCE OWNED BY; Schema: clinica; Owner: postgres
--

ALTER SEQUENCE clinica.receta_id_receta_seq OWNED BY clinica.receta.id_receta;


--
-- TOC entry 4789 (class 2604 OID 16622)
-- Name: audit_log id_log; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.audit_log ALTER COLUMN id_log SET DEFAULT nextval('clinica.audit_log_id_log_seq'::regclass);


--
-- TOC entry 4786 (class 2604 OID 16669)
-- Name: cita id_cita; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.cita ALTER COLUMN id_cita SET DEFAULT nextval('clinica.cita_id_cita_seq'::regclass);


--
-- TOC entry 4785 (class 2604 OID 16554)
-- Name: doc_especialidad id_doc_especialidad; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doc_especialidad ALTER COLUMN id_doc_especialidad SET DEFAULT nextval('clinica.doc_especialidad_id_doc_especialidad_seq'::regclass);


--
-- TOC entry 4783 (class 2604 OID 16699)
-- Name: doctor id_doctor; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doctor ALTER COLUMN id_doctor SET DEFAULT nextval('clinica.doctor_id_doctor_seq'::regclass);


--
-- TOC entry 4782 (class 2604 OID 16718)
-- Name: especialidad id_especialidad; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.especialidad ALTER COLUMN id_especialidad SET DEFAULT nextval('clinica.especialidad_id_especialidad_seq'::regclass);


--
-- TOC entry 4787 (class 2604 OID 16730)
-- Name: historial_medico id_historial; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.historial_medico ALTER COLUMN id_historial SET DEFAULT nextval('clinica.historial_medico_id_historial_seq'::regclass);


--
-- TOC entry 4784 (class 2604 OID 16636)
-- Name: paciente id_paciente; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.paciente ALTER COLUMN id_paciente SET DEFAULT nextval('clinica.paciente_id_paciente_seq'::regclass);


--
-- TOC entry 4788 (class 2604 OID 16766)
-- Name: receta id_receta; Type: DEFAULT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.receta ALTER COLUMN id_receta SET DEFAULT nextval('clinica.receta_id_receta_seq'::regclass);


--
-- TOC entry 4982 (class 0 OID 16619)
-- Dependencies: 233
-- Data for Name: audit_log; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.audit_log (id_log, operacion, tabla_nombre, pk_valor, usuario_nombre, old_data, new_data, fecha) FROM stdin;
1	INSERT	paciente	5	postgres	\N	{"sexo": "M", "correo": "juanperez@example.com", "nombre": "Juan", "apellido": "Pérez", "telefono": "8888-8888", "direccion": "Cartago", "id_paciente": 5, "fecha_nacimiento": "1990-05-10"}	2025-10-01 19:00:41.946993
2	DELETE	paciente	5	postgres	{"sexo": "M", "correo": "juanperez@example.com", "nombre": "Juan", "apellido": "Pérez", "telefono": "8888-8888", "direccion": "Cartago", "id_paciente": 5, "fecha_nacimiento": "1990-05-10"}	\N	2025-10-01 19:33:33.459396
3	INSERT	paciente	6	postgres	\N	{"sexo": "M", "correo": "juanperez@example.com", "nombre": "Juan", "apellido": "Pérez", "telefono": "8888-8888", "direccion": "Cartago", "id_paciente": 6, "fecha_nacimiento": "1990-05-10"}	2025-10-01 19:36:11.266438
4	DELETE	paciente	6	postgres	{"sexo": "M", "correo": "juanperez@example.com", "nombre": "Juan", "apellido": "Pérez", "telefono": "8888-8888", "direccion": "Cartago", "id_paciente": 6, "fecha_nacimiento": "1990-05-10"}	\N	2025-10-01 19:36:26.52219
5	INSERT	paciente	7	postgres	\N	{"sexo": "M", "correo": "juanperez@example.com", "nombre": "Juan", "apellido": "Pérez", "telefono": "8888-8888", "direccion": "Cartago", "id_paciente": 7, "fecha_nacimiento": "1990-05-10"}	2025-10-07 20:53:48.247267
6	INSERT	doctor	1	postgres	\N	{"nombre": "Daniel", "apellido": "Barboza", "telefono": "8888-9999", "direccion": "Avenida Central, Cartago", "id_doctor": 1, "especialidad_principal": "Medicina General"}	2025-10-07 23:13:36.057126
7	INSERT	especialidad	1	postgres	\N	{"id_especialidad": 1, "nom_especialidad": "Medicina General"}	2025-10-08 11:04:04.271317
8	INSERT	doc_especialidad	1	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 1}	2025-10-08 11:04:56.797134
9	UPDATE	doctor	1	postgres	{"nombre": "Daniel", "apellido": "Barboza", "telefono": "8888-9999", "direccion": "Avenida Central, Cartago", "id_doctor": 1}	{"nombre": "Daniel", "apellido": "Barboza", "telefono": "8888-0000", "direccion": "Avenida Central, Cartago", "id_doctor": 1}	2025-10-08 12:09:46.358602
10	INSERT	especialidad	2	postgres	\N	{"id_especialidad": 2, "nom_especialidad": "Cardiología"}	2025-10-08 12:34:05.979716
11	DELETE	doc_especialidad	1	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 1}	\N	2025-10-08 12:34:21.016449
12	INSERT	doc_especialidad	2	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 2}	2025-10-08 12:34:21.016449
13	DELETE	doc_especialidad	2	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 2}	\N	2025-10-08 12:34:27.594249
14	INSERT	doc_especialidad	3	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 3}	2025-10-08 12:34:27.594249
15	DELETE	doc_especialidad	3	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 3}	\N	2025-10-08 12:43:12.524261
16	INSERT	doc_especialidad	4	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 4}	2025-10-08 12:43:12.524261
17	DELETE	doc_especialidad	4	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 4}	\N	2025-10-08 12:43:15.657583
18	INSERT	doc_especialidad	5	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 5}	2025-10-08 12:43:15.657583
19	DELETE	doc_especialidad	5	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 5}	\N	2025-10-08 12:49:15.78901
20	INSERT	doc_especialidad	6	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 6}	2025-10-08 12:49:15.78901
21	INSERT	doc_especialidad	7	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 7}	2025-10-08 12:49:15.78901
22	DELETE	doc_especialidad	6	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 6}	\N	2025-10-08 12:49:20.636501
23	DELETE	doc_especialidad	7	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 7}	\N	2025-10-08 12:49:20.636501
24	INSERT	doc_especialidad	8	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 8}	2025-10-08 12:49:20.636501
25	DELETE	doc_especialidad	8	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 8}	\N	2025-10-08 13:22:34.688786
26	INSERT	doc_especialidad	9	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 9}	2025-10-08 13:23:14.463414
27	INSERT	doc_especialidad	10	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 10}	2025-10-08 13:23:14.463414
28	DELETE	doc_especialidad	9	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 9}	\N	2025-10-08 13:23:19.109161
29	DELETE	doc_especialidad	10	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 10}	\N	2025-10-08 13:23:19.109161
30	INSERT	doc_especialidad	11	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 11}	2025-10-08 13:23:35.008135
31	INSERT	doc_especialidad	12	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 12}	2025-10-08 13:23:35.008135
32	DELETE	doc_especialidad	11	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 11}	\N	2025-10-08 13:24:51.460885
33	DELETE	doc_especialidad	12	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 12}	\N	2025-10-08 13:24:51.460885
34	INSERT	doc_especialidad	13	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 13}	2025-10-08 13:24:51.460885
35	INSERT	especialidad	3	postgres	\N	{"id_especialidad": 3, "nom_especialidad": "Pediatría"}	2025-10-08 13:25:06.282186
36	DELETE	doc_especialidad	13	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 13}	\N	2025-10-08 13:25:11.904406
37	INSERT	doc_especialidad	14	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 14}	2025-10-08 13:25:11.904406
38	INSERT	doc_especialidad	15	postgres	\N	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 15}	2025-10-08 13:25:11.904406
39	INSERT	doc_especialidad	16	postgres	\N	{"id_doctor": 1, "id_especialidad": 3, "id_doc_especialidad": 16}	2025-10-08 13:25:11.904406
40	DELETE	doc_especialidad	14	postgres	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 14}	\N	2025-10-08 13:25:14.922016
41	DELETE	doc_especialidad	15	postgres	{"id_doctor": 1, "id_especialidad": 2, "id_doc_especialidad": 15}	\N	2025-10-08 13:25:14.922016
42	DELETE	doc_especialidad	16	postgres	{"id_doctor": 1, "id_especialidad": 3, "id_doc_especialidad": 16}	\N	2025-10-08 13:25:14.922016
43	INSERT	doc_especialidad	17	postgres	\N	{"id_doctor": 1, "id_especialidad": 1, "id_doc_especialidad": 17}	2025-10-08 13:25:14.922016
\.


--
-- TOC entry 4976 (class 0 OID 16568)
-- Dependencies: 227
-- Data for Name: cita; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.cita (id_cita, fecha_hora, id_paciente, id_doctor, motivo) FROM stdin;
\.


--
-- TOC entry 4974 (class 0 OID 16551)
-- Dependencies: 225
-- Data for Name: doc_especialidad; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.doc_especialidad (id_doc_especialidad, id_doctor, id_especialidad) FROM stdin;
17	1	1
\.


--
-- TOC entry 4970 (class 0 OID 16534)
-- Dependencies: 221
-- Data for Name: doctor; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.doctor (id_doctor, nombre, apellido, direccion, telefono) FROM stdin;
1	Daniel	Barboza	Avenida Central, Cartago	8888-0000
\.


--
-- TOC entry 4968 (class 0 OID 16527)
-- Dependencies: 219
-- Data for Name: especialidad; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.especialidad (id_especialidad, nom_especialidad) FROM stdin;
1	Medicina General
2	Cardiología
3	Pediatría
\.


--
-- TOC entry 4978 (class 0 OID 16585)
-- Dependencies: 229
-- Data for Name: historial_medico; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.historial_medico (id_historial, id_paciente, id_cita, fecha_consulta, diagnostico, tratamiento) FROM stdin;
\.


--
-- TOC entry 4972 (class 0 OID 16541)
-- Dependencies: 223
-- Data for Name: paciente; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.paciente (id_paciente, nombre, apellido, fecha_nacimiento, sexo, direccion, telefono, correo) FROM stdin;
7	Juan	Pérez	1990-05-10	M	Cartago	8888-8888	juanperez@example.com
\.


--
-- TOC entry 4980 (class 0 OID 16604)
-- Dependencies: 231
-- Data for Name: receta; Type: TABLE DATA; Schema: clinica; Owner: postgres
--

COPY clinica.receta (id_receta, medicamento, dosis, frecuencia, duracion, id_historial) FROM stdin;
\.


--
-- TOC entry 4996 (class 0 OID 0)
-- Dependencies: 232
-- Name: audit_log_id_log_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.audit_log_id_log_seq', 43, true);


--
-- TOC entry 4997 (class 0 OID 0)
-- Dependencies: 226
-- Name: cita_id_cita_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.cita_id_cita_seq', 1, false);


--
-- TOC entry 4998 (class 0 OID 0)
-- Dependencies: 224
-- Name: doc_especialidad_id_doc_especialidad_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.doc_especialidad_id_doc_especialidad_seq', 17, true);


--
-- TOC entry 4999 (class 0 OID 0)
-- Dependencies: 220
-- Name: doctor_id_doctor_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.doctor_id_doctor_seq', 1, true);


--
-- TOC entry 5000 (class 0 OID 0)
-- Dependencies: 218
-- Name: especialidad_id_especialidad_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.especialidad_id_especialidad_seq', 3, true);


--
-- TOC entry 5001 (class 0 OID 0)
-- Dependencies: 228
-- Name: historial_medico_id_historial_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.historial_medico_id_historial_seq', 1, false);


--
-- TOC entry 5002 (class 0 OID 0)
-- Dependencies: 222
-- Name: paciente_id_paciente_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.paciente_id_paciente_seq', 7, true);


--
-- TOC entry 5003 (class 0 OID 0)
-- Dependencies: 230
-- Name: receta_id_receta_seq; Type: SEQUENCE SET; Schema: clinica; Owner: postgres
--

SELECT pg_catalog.setval('clinica.receta_id_receta_seq', 1, false);


--
-- TOC entry 4807 (class 2606 OID 16627)
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id_log);


--
-- TOC entry 4801 (class 2606 OID 16671)
-- Name: cita cita_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.cita
    ADD CONSTRAINT cita_pkey PRIMARY KEY (id_cita);


--
-- TOC entry 4799 (class 2606 OID 16801)
-- Name: doc_especialidad doc_especialidad_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doc_especialidad
    ADD CONSTRAINT doc_especialidad_pkey PRIMARY KEY (id_doctor, id_especialidad);


--
-- TOC entry 4795 (class 2606 OID 16701)
-- Name: doctor doctor_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doctor
    ADD CONSTRAINT doctor_pkey PRIMARY KEY (id_doctor);


--
-- TOC entry 4793 (class 2606 OID 16720)
-- Name: especialidad especialidad_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.especialidad
    ADD CONSTRAINT especialidad_pkey PRIMARY KEY (id_especialidad);


--
-- TOC entry 4803 (class 2606 OID 16732)
-- Name: historial_medico historial_medico_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.historial_medico
    ADD CONSTRAINT historial_medico_pkey PRIMARY KEY (id_historial);


--
-- TOC entry 4797 (class 2606 OID 16638)
-- Name: paciente paciente_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.paciente
    ADD CONSTRAINT paciente_pkey PRIMARY KEY (id_paciente);


--
-- TOC entry 4805 (class 2606 OID 16768)
-- Name: receta receta_pkey; Type: CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.receta
    ADD CONSTRAINT receta_pkey PRIMARY KEY (id_receta);


--
-- TOC entry 4819 (class 2620 OID 16633)
-- Name: cita audit_cita; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_cita AFTER INSERT OR DELETE OR UPDATE ON clinica.cita FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_cita');


--
-- TOC entry 4818 (class 2620 OID 16632)
-- Name: doc_especialidad audit_doc_especialidad; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_doc_especialidad AFTER INSERT OR DELETE OR UPDATE ON clinica.doc_especialidad FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_doc_especialidad');


--
-- TOC entry 4816 (class 2620 OID 16630)
-- Name: doctor audit_doctor; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_doctor AFTER INSERT OR DELETE OR UPDATE ON clinica.doctor FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_doctor');


--
-- TOC entry 4815 (class 2620 OID 16631)
-- Name: especialidad audit_especialidad; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_especialidad AFTER INSERT OR DELETE OR UPDATE ON clinica.especialidad FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_especialidad');


--
-- TOC entry 4820 (class 2620 OID 16634)
-- Name: historial_medico audit_historial; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_historial AFTER INSERT OR DELETE OR UPDATE ON clinica.historial_medico FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_historial');


--
-- TOC entry 4817 (class 2620 OID 16629)
-- Name: paciente audit_paciente; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_paciente AFTER INSERT OR DELETE OR UPDATE ON clinica.paciente FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_paciente');


--
-- TOC entry 4821 (class 2620 OID 16635)
-- Name: receta audit_receta; Type: TRIGGER; Schema: clinica; Owner: postgres
--

CREATE TRIGGER audit_receta AFTER INSERT OR DELETE OR UPDATE ON clinica.receta FOR EACH ROW EXECUTE FUNCTION clinica.audit_trigger('id_receta');


--
-- TOC entry 4810 (class 2606 OID 16707)
-- Name: cita cita_id_doctor_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.cita
    ADD CONSTRAINT cita_id_doctor_fkey FOREIGN KEY (id_doctor) REFERENCES clinica.doctor(id_doctor);


--
-- TOC entry 4811 (class 2606 OID 16690)
-- Name: cita cita_id_paciente_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.cita
    ADD CONSTRAINT cita_id_paciente_fkey FOREIGN KEY (id_paciente) REFERENCES clinica.paciente(id_paciente);


--
-- TOC entry 4808 (class 2606 OID 16782)
-- Name: doc_especialidad doc_especialidad_id_doctor_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doc_especialidad
    ADD CONSTRAINT doc_especialidad_id_doctor_fkey FOREIGN KEY (id_doctor) REFERENCES clinica.doctor(id_doctor);


--
-- TOC entry 4809 (class 2606 OID 16791)
-- Name: doc_especialidad doc_especialidad_id_especialidad_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.doc_especialidad
    ADD CONSTRAINT doc_especialidad_id_especialidad_fkey FOREIGN KEY (id_especialidad) REFERENCES clinica.especialidad(id_especialidad);


--
-- TOC entry 4812 (class 2606 OID 16744)
-- Name: historial_medico historial_medico_id_cita_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.historial_medico
    ADD CONSTRAINT historial_medico_id_cita_fkey FOREIGN KEY (id_cita) REFERENCES clinica.cita(id_cita);


--
-- TOC entry 4813 (class 2606 OID 16755)
-- Name: historial_medico historial_medico_id_paciente_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.historial_medico
    ADD CONSTRAINT historial_medico_id_paciente_fkey FOREIGN KEY (id_paciente) REFERENCES clinica.paciente(id_paciente);


--
-- TOC entry 4814 (class 2606 OID 16773)
-- Name: receta receta_id_historial_fkey; Type: FK CONSTRAINT; Schema: clinica; Owner: postgres
--

ALTER TABLE ONLY clinica.receta
    ADD CONSTRAINT receta_id_historial_fkey FOREIGN KEY (id_historial) REFERENCES clinica.historial_medico(id_historial);


-- Completed on 2025-10-08 14:35:07

--
-- PostgreSQL database dump complete
--

\unrestrict t1401fcLg2Usi2cnc0nbGmxwo6vcfujaZtBubBVdNGdv7flEWsHunX9v96E5dhA

