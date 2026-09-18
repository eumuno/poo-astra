--
-- PostgreSQL database dump
--

\restrict 0RTTeuAn4q8cfnk1cuVjF8mZS3EVVUHD3Zgx0eGnpe7mOdOCGBRHFMaFMsN1e7c

-- Dumped from database version 14.24 (Homebrew)
-- Dumped by pg_dump version 14.24 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: filmes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filmes (
    id integer NOT NULL,
    titulo character varying(200) NOT NULL,
    diretor character varying(100),
    ano_lancamento integer,
    genero character varying(50),
    duracao_minutos integer,
    sinopse text,
    criado_em timestamp without time zone DEFAULT now()
);


ALTER TABLE public.filmes OWNER TO postgres;

--
-- Name: filmes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.filmes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.filmes_id_seq OWNER TO postgres;

--
-- Name: filmes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.filmes_id_seq OWNED BY public.filmes.id;


--
-- Name: filmes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filmes ALTER COLUMN id SET DEFAULT nextval('public.filmes_id_seq'::regclass);


--
-- Name: filmes filmes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filmes
    ADD CONSTRAINT filmes_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict 0RTTeuAn4q8cfnk1cuVjF8mZS3EVVUHD3Zgx0eGnpe7mOdOCGBRHFMaFMsN1e7c

