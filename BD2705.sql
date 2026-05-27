--
-- PostgreSQL database dump
--

\restrict AuI4rSzOQNCcZYSKDGfeJTbrDKWQlerFFlkQ0zKcfMyjr9as6wQq3cB0mXIfaMO

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

-- Started on 2026-05-27 23:59:20

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 33565)
-- Name: answer_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.answer_options (
    id integer NOT NULL,
    question_id integer,
    text character varying(500) NOT NULL,
    order_num integer NOT NULL
);


ALTER TABLE public.answer_options OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 33573)
-- Name: answer_options_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.answer_options_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.answer_options_id_seq OWNER TO postgres;

--
-- TOC entry 5189 (class 0 OID 0)
-- Dependencies: 220
-- Name: answer_options_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.answer_options_id_seq OWNED BY public.answer_options.id;


--
-- TOC entry 221 (class 1259 OID 33574)
-- Name: answer_parameter_impact; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.answer_parameter_impact (
    id integer NOT NULL,
    answer_option_id integer,
    parameter_id integer,
    delta integer NOT NULL
);


ALTER TABLE public.answer_parameter_impact OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 33579)
-- Name: answer_parameter_impact_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.answer_parameter_impact_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.answer_parameter_impact_id_seq OWNER TO postgres;

--
-- TOC entry 5190 (class 0 OID 0)
-- Dependencies: 222
-- Name: answer_parameter_impact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.answer_parameter_impact_id_seq OWNED BY public.answer_parameter_impact.id;


--
-- TOC entry 223 (class 1259 OID 33580)
-- Name: groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.groups (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text
);


ALTER TABLE public.groups OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 33587)
-- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.groups_id_seq OWNER TO postgres;

--
-- TOC entry 5191 (class 0 OID 0)
-- Dependencies: 224
-- Name: groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.groups_id_seq OWNED BY public.groups.id;


--
-- TOC entry 225 (class 1259 OID 33588)
-- Name: parameter_interpretations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parameter_interpretations (
    id integer NOT NULL,
    parameter_id integer,
    range_start integer,
    range_end integer,
    binary_value character varying(10),
    interpretation_text text NOT NULL
);


ALTER TABLE public.parameter_interpretations OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 33595)
-- Name: parameter_interpretations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parameter_interpretations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parameter_interpretations_id_seq OWNER TO postgres;

--
-- TOC entry 5192 (class 0 OID 0)
-- Dependencies: 226
-- Name: parameter_interpretations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parameter_interpretations_id_seq OWNED BY public.parameter_interpretations.id;


--
-- TOC entry 227 (class 1259 OID 33596)
-- Name: parameters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parameters (
    id integer NOT NULL,
    test_id integer,
    name character varying(100) NOT NULL,
    scale_type character varying(20) DEFAULT 'RANGE'::character varying,
    min_value integer DEFAULT 0,
    max_value integer DEFAULT 0
);


ALTER TABLE public.parameters OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 33604)
-- Name: parameters_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parameters_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parameters_id_seq OWNER TO postgres;

--
-- TOC entry 5193 (class 0 OID 0)
-- Dependencies: 228
-- Name: parameters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parameters_id_seq OWNED BY public.parameters.id;


--
-- TOC entry 229 (class 1259 OID 33605)
-- Name: questions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.questions (
    id integer NOT NULL,
    test_id integer,
    text text NOT NULL,
    order_num integer NOT NULL
);


ALTER TABLE public.questions OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 33613)
-- Name: questions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.questions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.questions_id_seq OWNER TO postgres;

--
-- TOC entry 5194 (class 0 OID 0)
-- Dependencies: 230
-- Name: questions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.questions_id_seq OWNED BY public.questions.id;


--
-- TOC entry 231 (class 1259 OID 33614)
-- Name: test_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_assignments (
    id integer NOT NULL,
    test_id integer,
    assigned_by integer,
    assigned_to_user integer,
    assigned_to_group integer,
    assigned_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    due_date date
);


ALTER TABLE public.test_assignments OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 33619)
-- Name: test_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.test_assignments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.test_assignments_id_seq OWNER TO postgres;

--
-- TOC entry 5195 (class 0 OID 0)
-- Dependencies: 232
-- Name: test_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.test_assignments_id_seq OWNED BY public.test_assignments.id;


--
-- TOC entry 233 (class 1259 OID 33620)
-- Name: test_drafts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_drafts (
    id integer NOT NULL,
    teacher_id integer,
    test_data jsonb NOT NULL,
    last_saved timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_completed boolean DEFAULT false,
    original_test_id integer
);


ALTER TABLE public.test_drafts OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 33629)
-- Name: test_drafts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.test_drafts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.test_drafts_id_seq OWNER TO postgres;

--
-- TOC entry 5196 (class 0 OID 0)
-- Dependencies: 234
-- Name: test_drafts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.test_drafts_id_seq OWNED BY public.test_drafts.id;


--
-- TOC entry 235 (class 1259 OID 33630)
-- Name: test_results; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_results (
    id integer NOT NULL,
    session_id integer,
    parameter_id integer,
    raw_score integer,
    interpreted_code character varying(10),
    interpretation_text text,
    scaled_score integer
);


ALTER TABLE public.test_results OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 33636)
-- Name: test_results_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.test_results_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.test_results_id_seq OWNER TO postgres;

--
-- TOC entry 5197 (class 0 OID 0)
-- Dependencies: 236
-- Name: test_results_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.test_results_id_seq OWNED BY public.test_results.id;


--
-- TOC entry 237 (class 1259 OID 33637)
-- Name: test_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_sessions (
    id integer NOT NULL,
    user_id integer,
    test_id integer,
    start_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    end_time timestamp without time zone,
    status character varying(20) DEFAULT 'IN_PROGRESS'::character varying
);


ALTER TABLE public.test_sessions OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 33643)
-- Name: test_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.test_sessions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.test_sessions_id_seq OWNER TO postgres;

--
-- TOC entry 5198 (class 0 OID 0)
-- Dependencies: 238
-- Name: test_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.test_sessions_id_seq OWNED BY public.test_sessions.id;


--
-- TOC entry 239 (class 1259 OID 33644)
-- Name: tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tests (
    id integer NOT NULL,
    name character varying(200) NOT NULL,
    description text,
    created_by integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    questions_per_session text
);


ALTER TABLE public.tests OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 33652)
-- Name: tests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tests_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tests_id_seq OWNER TO postgres;

--
-- TOC entry 5199 (class 0 OID 0)
-- Dependencies: 240
-- Name: tests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tests_id_seq OWNED BY public.tests.id;


--
-- TOC entry 241 (class 1259 OID 33653)
-- Name: user_answers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_answers (
    id integer NOT NULL,
    session_id integer,
    question_id integer,
    answer_option_id integer,
    answered_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_answers OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 33658)
-- Name: user_answers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_answers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_answers_id_seq OWNER TO postgres;

--
-- TOC entry 5200 (class 0 OID 0)
-- Dependencies: 242
-- Name: user_answers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_answers_id_seq OWNED BY public.user_answers.id;


--
-- TOC entry 243 (class 1259 OID 33659)
-- Name: user_groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_groups (
    user_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public.user_groups OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 33664)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    full_name character varying(100),
    role character varying(20) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('TEACHER'::character varying)::text, ('TAKER'::character varying)::text])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 33673)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- TOC entry 5201 (class 0 OID 0)
-- Dependencies: 245
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 4920 (class 2604 OID 33674)
-- Name: answer_options id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_options ALTER COLUMN id SET DEFAULT nextval('public.answer_options_id_seq'::regclass);


--
-- TOC entry 4921 (class 2604 OID 33675)
-- Name: answer_parameter_impact id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_parameter_impact ALTER COLUMN id SET DEFAULT nextval('public.answer_parameter_impact_id_seq'::regclass);


--
-- TOC entry 4922 (class 2604 OID 33676)
-- Name: groups id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.groups ALTER COLUMN id SET DEFAULT nextval('public.groups_id_seq'::regclass);


--
-- TOC entry 4923 (class 2604 OID 33677)
-- Name: parameter_interpretations id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter_interpretations ALTER COLUMN id SET DEFAULT nextval('public.parameter_interpretations_id_seq'::regclass);


--
-- TOC entry 4924 (class 2604 OID 33678)
-- Name: parameters id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameters ALTER COLUMN id SET DEFAULT nextval('public.parameters_id_seq'::regclass);


--
-- TOC entry 4928 (class 2604 OID 33679)
-- Name: questions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions ALTER COLUMN id SET DEFAULT nextval('public.questions_id_seq'::regclass);


--
-- TOC entry 4929 (class 2604 OID 33680)
-- Name: test_assignments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments ALTER COLUMN id SET DEFAULT nextval('public.test_assignments_id_seq'::regclass);


--
-- TOC entry 4931 (class 2604 OID 33681)
-- Name: test_drafts id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drafts ALTER COLUMN id SET DEFAULT nextval('public.test_drafts_id_seq'::regclass);


--
-- TOC entry 4934 (class 2604 OID 33682)
-- Name: test_results id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_results ALTER COLUMN id SET DEFAULT nextval('public.test_results_id_seq'::regclass);


--
-- TOC entry 4935 (class 2604 OID 33683)
-- Name: test_sessions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_sessions ALTER COLUMN id SET DEFAULT nextval('public.test_sessions_id_seq'::regclass);


--
-- TOC entry 4938 (class 2604 OID 33684)
-- Name: tests id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests ALTER COLUMN id SET DEFAULT nextval('public.tests_id_seq'::regclass);


--
-- TOC entry 4940 (class 2604 OID 33685)
-- Name: user_answers id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_answers ALTER COLUMN id SET DEFAULT nextval('public.user_answers_id_seq'::regclass);


--
-- TOC entry 4942 (class 2604 OID 33686)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 5157 (class 0 OID 33565)
-- Dependencies: 219
-- Data for Name: answer_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.answer_options (id, question_id, text, order_num) FROM stdin;
13	7	А) Встретиться с друзьями, поговорить, куда-нибудь сходить	0
14	7	Б) Побыть дома в тишине, восстановить силы	1
15	8	А) Быстро расстраиваетесь или раздражаетесь	0
16	8	Б) Воспринимаете спокойно, ищете решение	1
17	9	А) Легко начинаете разговор, знакомитесь сами	0
18	9	Б) Держитесь в стороне, пока не освоитесь	1
19	10	А) Долго не можете успокоиться, мысли возвращаются снова и снова	0
20	10	Б) Разобрались — забыли, не носите переживания в себе	1
21	11	А) В центре событий: шутите, танцуете, поднимаете настроение	0
22	11	Б) Общаетесь с 1–2 близкими людьми в уголке	1
23	12	А) Обижаетесь или заводитесь, сложно сразу принят	0
24	12	Б) Выслушиваете спокойно, берёте то, что полезно	1
25	13	А) С удовольствием — вам нравится выступать публично	0
26	13	Б) Стараетесь избежать — публичность вас напрягает	1
27	14	А) Заметно волнуетесь: сложно спать, мысли роятся	0
28	14	Б) Готовитесь спокойно, не теряете сон из-за ожидания	1
29	15	А) Работа в команде, мозговые штурмы, активное взаимодействие	0
30	15	Б) Самостоятельная сосредоточенная работа без отвлечений	1
31	16	А) Довольно часто — может переключаться несколько раз в день	0
32	16	Б) Редко — ваше состояние стабильно изо дня в день	1
73	37	Да	0
74	37	Нет	1
75	37	Не знаю	2
76	38	Да	0
77	38	Нет	1
78	38	Не знаю	2
79	39	Да	0
80	39	Нет	1
81	40	Вы тевожный? (да)	0
82	40	Нет	1
103	46	Да	0
104	46	Скорее да, чем нет	1
105	46	Не могу ответить точно	2
106	46	Нет	3
107	47	Да	0
108	47	Скорее да, чем нет	1
109	47	Не могу дать точный ответ	2
110	47	Нет	3
111	48	Да	0
112	48	Скорее да, чем нет	1
113	48	Не могу дать точного ответа	2
114	48	Нет	3
115	49	Да	0
116	49	Скорее да, чем нет	1
117	49	Не могу дать точного ответа	2
118	49	Нет	3
119	50	Да	0
120	50	Скорее да, чем нет	1
121	50	Не могу дать точного ответа	2
122	50	Нет	3
\.


--
-- TOC entry 5159 (class 0 OID 33574)
-- Dependencies: 221
-- Data for Name: answer_parameter_impact; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.answer_parameter_impact (id, answer_option_id, parameter_id, delta) FROM stdin;
3	13	4	1
4	14	4	-1
5	15	5	1
6	16	5	-1
7	17	4	1
8	18	4	-1
9	19	5	1
10	20	5	-1
11	21	4	1
12	22	4	-1
13	23	5	1
14	24	5	-1
15	25	4	1
16	26	4	-1
17	27	5	1
18	28	5	-1
19	29	4	1
20	30	4	-1
21	31	5	1
22	32	5	-1
63	73	10	10
64	73	11	1
65	75	10	10
66	76	10	10
67	76	11	1
68	78	10	5
69	79	10	10
70	81	10	10
86	103	13	20
87	104	13	10
88	105	13	5
89	107	13	20
90	108	13	10
91	109	13	5
92	111	13	20
93	112	13	10
94	113	13	5
95	115	13	20
96	116	13	10
97	117	13	5
98	119	13	20
99	120	13	10
100	121	13	5
\.


--
-- TOC entry 5161 (class 0 OID 33580)
-- Dependencies: 223
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.groups (id, name, description) FROM stdin;
1	ИСТб-24-2	Группа студентов ИРНИТУ
\.


--
-- TOC entry 5163 (class 0 OID 33588)
-- Dependencies: 225
-- Data for Name: parameter_interpretations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parameter_interpretations (id, parameter_id, range_start, range_end, binary_value, interpretation_text) FROM stdin;
13	4	\N	\N	SCORE <= 0	Интроверт — черпаете энергию из уединения, предпочитаете глубокое общение с 1–2 людьми, хорошо работаете самостоятельно.
14	4	\N	\N	SCORE > 0	Экстраверт — заряжаетесь от общения, легко знакомитесь с новыми людьми, комфортно чувствуете себя в центре внимания.
15	5	\N	\N	SCORE <= 0	Эмоционально устойчивый — спокойно реагируете на стресс, редко теряете равновесие, принимаете решения взвешенно.
16	5	\N	\N	SCORE > 0	Эмоционально чувствительный — живо реагируете на происходящее, остро переживаете неудачи, но и радуетесь искренне.
25	10	0	24	\N	нет тревожности
26	10	25	50	\N	слабая тревожность
27	10	51	74	\N	выраженная тревожность
28	10	75	100	\N	высокая тревожность
29	11	\N	\N	SCORE <= 0	нет депресии
30	11	\N	\N	SCORE > 0	есть деспресия
34	13	0	24	\N	нет тревожности
35	13	25	65	\N	слабая тревожность
36	13	66	100	\N	сильная тревожность
\.


--
-- TOC entry 5165 (class 0 OID 33596)
-- Dependencies: 227
-- Data for Name: parameters; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parameters (id, test_id, name, scale_type, min_value, max_value) FROM stdin;
4	4	ЭКСТРАВЕРСИЯ	BINARY	0	0
5	4	НЕЙРОТИЗМ	BINARY	0	0
10	6	ТРЕВОЖНОСТЬ	RANGE	0	0
11	6	ДЕПРЕСИВНОСТЬ	BINARY	0	0
13	7	ТРЕВОЖНОСТЬ	RANGE	0	0
\.


--
-- TOC entry 5167 (class 0 OID 33605)
-- Dependencies: 229
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.questions (id, test_id, text, order_num) FROM stdin;
7	4	Как вы предпочитаете проводить вечер после насыщённого дня?	0
8	4	Если на учёбе или работе что-то пошло не так…	1
9	4	В незнакомой компании вы…	2
10	4	Когда вы переживаете из-за проблемы…	3
11	4	На вечеринке или корпоративе вы скорее…	4
12	4	Как вы реагируете на критику в свой адрес?	5
13	4	Вам предложили выступить перед аудиторией. Ваша реакция?	6
14	4	Перед важным событием вы…	7
15	4	Ваш идеальный рабочий процесс:	8
16	4	Как часто у вас резко меняется настроение?	9
37	6	У вас есть депресия?	0
38	6	Вы тревожны?	1
39	6	Вы тревожный? 2	2
40	6	Да	3
46	7	Боитесь ли вы сдавать экзамен?	0
47	7	Вам некомфортно ездить в общественном транспорте, так как там слишком людей?	1
48	7	Вы боитесь лишний раз выходить из дома?	2
49	7	Вы всегда приходите раньше заплонированного срока?	3
50	7	Вы боитесь, что в вашей внешности, что-то не так, но вы этого не можете заметить?	4
\.


--
-- TOC entry 5169 (class 0 OID 33614)
-- Dependencies: 231
-- Data for Name: test_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.test_assignments (id, test_id, assigned_by, assigned_to_user, assigned_to_group, assigned_at, due_date) FROM stdin;
7	4	3	\N	1	2026-05-25 08:07:16.590416	\N
8	6	3	4	\N	2026-05-25 11:06:08.085919	\N
9	7	3	\N	1	2026-05-25 13:11:04.041715	\N
\.


--
-- TOC entry 5171 (class 0 OID 33620)
-- Dependencies: 233
-- Data for Name: test_drafts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.test_drafts (id, teacher_id, test_data, last_saved, is_completed, original_test_id) FROM stdin;
\.


--
-- TOC entry 5173 (class 0 OID 33630)
-- Dependencies: 235
-- Data for Name: test_results; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.test_results (id, session_id, parameter_id, raw_score, interpreted_code, interpretation_text, scaled_score) FROM stdin;
6	19	4	-3	SCORE <= 0	Интроверт — черпаете энергию из уединения, предпочитаете глубокое общение с 1–2 людьми, хорошо работаете самостоятельно.	-3
7	19	5	-1	SCORE <= 0	Эмоционально устойчивый — спокойно реагируете на стресс, редко теряете равновесие, принимаете решения взвешенно.	-1
8	20	10	20	\N	нет тревожности	20
9	20	11	1	SCORE > 0	есть деспресия	1
10	21	13	35	\N	слабая тревожность	35
11	22	13	80	\N	сильная тревожность	80
\.


--
-- TOC entry 5175 (class 0 OID 33637)
-- Dependencies: 237
-- Data for Name: test_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.test_sessions (id, user_id, test_id, start_time, end_time, status) FROM stdin;
19	5	4	2026-05-25 08:15:43.155506	2026-05-25 08:16:25.566664	COMPLETED
20	4	6	2026-05-25 11:07:35.724121	2026-05-25 11:07:56.009101	COMPLETED
21	4	7	2026-05-25 13:12:09.078683	2026-05-25 13:12:34.884968	COMPLETED
22	5	7	2026-05-25 13:13:21.841986	2026-05-25 13:13:36.713951	COMPLETED
\.


--
-- TOC entry 5177 (class 0 OID 33644)
-- Dependencies: 239
-- Data for Name: tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tests (id, name, description, created_by, created_at, questions_per_session) FROM stdin;
4	Тип темперамента	Тест определяет ваш тип темперамента по двум осям — направленность личности и эмоциональная устойчивость. Основан на модели Г. Айзенка.	3	2026-05-25 08:06:36.503476	0
6	тест тревожность		3	2026-05-25 11:05:50.937509	0
7	тест на тревожность	методика определение тревожности	3	2026-05-25 12:06:52.092088	0
\.


--
-- TOC entry 5179 (class 0 OID 33653)
-- Dependencies: 241
-- Data for Name: user_answers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_answers (id, session_id, question_id, answer_option_id, answered_at) FROM stdin;
30	19	7	14	2026-05-25 08:15:48.496345
31	19	8	16	2026-05-25 08:15:50.70183
32	19	9	18	2026-05-25 08:15:53.093638
33	19	10	19	2026-05-25 08:15:57.042762
34	19	11	22	2026-05-25 08:15:59.178684
35	19	12	24	2026-05-25 08:16:02.594997
36	19	13	26	2026-05-25 08:16:05.463675
37	19	14	27	2026-05-25 08:16:10.774209
38	19	15	29	2026-05-25 08:16:13.891104
40	19	16	32	2026-05-25 08:16:20.009178
41	20	37	73	2026-05-25 11:07:41.193258
42	20	39	79	2026-05-25 11:07:47.069439
43	21	46	103	2026-05-25 13:12:14.751018
44	21	47	110	2026-05-25 13:12:18.05955
45	21	48	113	2026-05-25 13:12:21.865141
46	21	49	117	2026-05-25 13:12:25.589345
48	21	50	121	2026-05-25 13:12:29.145375
49	22	46	103	2026-05-25 13:13:26.018936
50	22	47	108	2026-05-25 13:13:27.688698
51	22	48	112	2026-05-25 13:13:29.096318
52	22	49	115	2026-05-25 13:13:31.031645
54	22	50	119	2026-05-25 13:13:32.748347
\.


--
-- TOC entry 5181 (class 0 OID 33659)
-- Dependencies: 243
-- Data for Name: user_groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_groups (user_id, group_id) FROM stdin;
4	1
5	1
\.


--
-- TOC entry 5182 (class 0 OID 33664)
-- Dependencies: 244
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash, full_name, role, created_at) FROM stdin;
2	admin	$2a$12$e7wFzvJCURSlad64HjYOae05yj/GtIEEVLUxATQTzlJR0FUDN7fy2	System Administrator	ADMIN	2026-05-20 10:47:52.676463
3	teacher	$2a$12$TToGm2Mpt8CvJPHszNZdUuyonFUYkbTcjnRdt4ALcd9zOx6aWjC1q	Маланова Татьяна Валерьевна	TEACHER	2026-05-20 10:55:25.383712
4	student	$2a$12$b3an..XdjYB059tNMSKwlO8e0HC8on9GMsZJrXyOe9FntTneRBeua	Иванов Даниил Александрович	TAKER	2026-05-20 10:56:06.328477
5	student2	$2a$12$CxfkM8uxSCOUmobfSL2/bOxfMDSWrf/c1PduTj5rFJy2fM8wtA94C	Петров Дмитрий Олегович	TAKER	2026-05-20 10:56:54.201623
6	teacher2	$2a$12$76sAudYo8jzFpWZdqafPgegJNY1mF./DMJabkzN74YWbkYHQpxdCa	Каташевцев Михаил Дмитриевич	TEACHER	2026-05-20 16:29:37.571836
\.


--
-- TOC entry 5202 (class 0 OID 0)
-- Dependencies: 220
-- Name: answer_options_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.answer_options_id_seq', 122, true);


--
-- TOC entry 5203 (class 0 OID 0)
-- Dependencies: 222
-- Name: answer_parameter_impact_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.answer_parameter_impact_id_seq', 100, true);


--
-- TOC entry 5204 (class 0 OID 0)
-- Dependencies: 224
-- Name: groups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.groups_id_seq', 1, true);


--
-- TOC entry 5205 (class 0 OID 0)
-- Dependencies: 226
-- Name: parameter_interpretations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parameter_interpretations_id_seq', 36, true);


--
-- TOC entry 5206 (class 0 OID 0)
-- Dependencies: 228
-- Name: parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parameters_id_seq', 13, true);


--
-- TOC entry 5207 (class 0 OID 0)
-- Dependencies: 230
-- Name: questions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.questions_id_seq', 50, true);


--
-- TOC entry 5208 (class 0 OID 0)
-- Dependencies: 232
-- Name: test_assignments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.test_assignments_id_seq', 9, true);


--
-- TOC entry 5209 (class 0 OID 0)
-- Dependencies: 234
-- Name: test_drafts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.test_drafts_id_seq', 1, false);


--
-- TOC entry 5210 (class 0 OID 0)
-- Dependencies: 236
-- Name: test_results_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.test_results_id_seq', 11, true);


--
-- TOC entry 5211 (class 0 OID 0)
-- Dependencies: 238
-- Name: test_sessions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.test_sessions_id_seq', 22, true);


--
-- TOC entry 5212 (class 0 OID 0)
-- Dependencies: 240
-- Name: tests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.tests_id_seq', 7, true);


--
-- TOC entry 5213 (class 0 OID 0)
-- Dependencies: 242
-- Name: user_answers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_answers_id_seq', 54, true);


--
-- TOC entry 5214 (class 0 OID 0)
-- Dependencies: 245
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 6, true);


--
-- TOC entry 4946 (class 2606 OID 33688)
-- Name: answer_options answer_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_options
    ADD CONSTRAINT answer_options_pkey PRIMARY KEY (id);


--
-- TOC entry 4949 (class 2606 OID 33690)
-- Name: answer_parameter_impact answer_parameter_impact_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_parameter_impact
    ADD CONSTRAINT answer_parameter_impact_pkey PRIMARY KEY (id);


--
-- TOC entry 4952 (class 2606 OID 33692)
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- TOC entry 4954 (class 2606 OID 33694)
-- Name: parameter_interpretations parameter_interpretations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter_interpretations
    ADD CONSTRAINT parameter_interpretations_pkey PRIMARY KEY (id);


--
-- TOC entry 4957 (class 2606 OID 33696)
-- Name: parameters parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameters
    ADD CONSTRAINT parameters_pkey PRIMARY KEY (id);


--
-- TOC entry 4960 (class 2606 OID 33698)
-- Name: questions questions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_pkey PRIMARY KEY (id);


--
-- TOC entry 4964 (class 2606 OID 33700)
-- Name: test_assignments test_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments
    ADD CONSTRAINT test_assignments_pkey PRIMARY KEY (id);


--
-- TOC entry 4967 (class 2606 OID 33702)
-- Name: test_drafts test_drafts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drafts
    ADD CONSTRAINT test_drafts_pkey PRIMARY KEY (id);


--
-- TOC entry 4970 (class 2606 OID 33704)
-- Name: test_results test_results_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_results
    ADD CONSTRAINT test_results_pkey PRIMARY KEY (id);


--
-- TOC entry 4976 (class 2606 OID 33706)
-- Name: test_sessions test_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_sessions
    ADD CONSTRAINT test_sessions_pkey PRIMARY KEY (id);


--
-- TOC entry 4978 (class 2606 OID 33708)
-- Name: tests tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT tests_pkey PRIMARY KEY (id);


--
-- TOC entry 4981 (class 2606 OID 33710)
-- Name: user_answers user_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_answers
    ADD CONSTRAINT user_answers_pkey PRIMARY KEY (id);


--
-- TOC entry 4983 (class 2606 OID 33712)
-- Name: user_groups user_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT user_groups_pkey PRIMARY KEY (user_id, group_id);


--
-- TOC entry 4985 (class 2606 OID 33714)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4987 (class 2606 OID 33716)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4947 (class 1259 OID 33717)
-- Name: idx_answer_options_question; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_answer_options_question ON public.answer_options USING btree (question_id);


--
-- TOC entry 4961 (class 1259 OID 33718)
-- Name: idx_assignments_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_assignments_group ON public.test_assignments USING btree (assigned_to_group);


--
-- TOC entry 4962 (class 1259 OID 33719)
-- Name: idx_assignments_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_assignments_user ON public.test_assignments USING btree (assigned_to_user);


--
-- TOC entry 4950 (class 1259 OID 33720)
-- Name: idx_impacts_answer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_impacts_answer ON public.answer_parameter_impact USING btree (answer_option_id);


--
-- TOC entry 4955 (class 1259 OID 33721)
-- Name: idx_parameters_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_parameters_test ON public.parameters USING btree (test_id);


--
-- TOC entry 4958 (class 1259 OID 33722)
-- Name: idx_questions_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_questions_test ON public.questions USING btree (test_id);


--
-- TOC entry 4971 (class 1259 OID 33723)
-- Name: idx_sessions_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_test ON public.test_sessions USING btree (test_id);


--
-- TOC entry 4972 (class 1259 OID 33724)
-- Name: idx_sessions_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessions_user ON public.test_sessions USING btree (user_id);


--
-- TOC entry 4965 (class 1259 OID 33725)
-- Name: idx_test_drafts_teacher; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_drafts_teacher ON public.test_drafts USING btree (teacher_id);


--
-- TOC entry 4968 (class 1259 OID 33726)
-- Name: idx_test_results_session; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_results_session ON public.test_results USING btree (session_id);


--
-- TOC entry 4973 (class 1259 OID 33727)
-- Name: idx_test_sessions_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_sessions_test ON public.test_sessions USING btree (test_id);


--
-- TOC entry 4974 (class 1259 OID 33728)
-- Name: idx_test_sessions_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_test_sessions_user ON public.test_sessions USING btree (user_id);


--
-- TOC entry 4979 (class 1259 OID 33729)
-- Name: idx_user_answers_session; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_answers_session ON public.user_answers USING btree (session_id);


--
-- TOC entry 4988 (class 2606 OID 33730)
-- Name: answer_options answer_options_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_options
    ADD CONSTRAINT answer_options_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id) ON DELETE CASCADE;


--
-- TOC entry 4989 (class 2606 OID 33735)
-- Name: answer_parameter_impact answer_parameter_impact_answer_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_parameter_impact
    ADD CONSTRAINT answer_parameter_impact_answer_option_id_fkey FOREIGN KEY (answer_option_id) REFERENCES public.answer_options(id) ON DELETE CASCADE;


--
-- TOC entry 4990 (class 2606 OID 33740)
-- Name: answer_parameter_impact answer_parameter_impact_parameter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_parameter_impact
    ADD CONSTRAINT answer_parameter_impact_parameter_id_fkey FOREIGN KEY (parameter_id) REFERENCES public.parameters(id) ON DELETE CASCADE;


--
-- TOC entry 4991 (class 2606 OID 33745)
-- Name: parameter_interpretations parameter_interpretations_parameter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameter_interpretations
    ADD CONSTRAINT parameter_interpretations_parameter_id_fkey FOREIGN KEY (parameter_id) REFERENCES public.parameters(id) ON DELETE CASCADE;


--
-- TOC entry 4992 (class 2606 OID 33750)
-- Name: parameters parameters_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parameters
    ADD CONSTRAINT parameters_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.tests(id) ON DELETE CASCADE;


--
-- TOC entry 4993 (class 2606 OID 33755)
-- Name: questions questions_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.questions
    ADD CONSTRAINT questions_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.tests(id) ON DELETE CASCADE;


--
-- TOC entry 4994 (class 2606 OID 33760)
-- Name: test_assignments test_assignments_assigned_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments
    ADD CONSTRAINT test_assignments_assigned_by_fkey FOREIGN KEY (assigned_by) REFERENCES public.users(id);


--
-- TOC entry 4995 (class 2606 OID 33765)
-- Name: test_assignments test_assignments_assigned_to_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments
    ADD CONSTRAINT test_assignments_assigned_to_group_fkey FOREIGN KEY (assigned_to_group) REFERENCES public.groups(id);


--
-- TOC entry 4996 (class 2606 OID 33770)
-- Name: test_assignments test_assignments_assigned_to_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments
    ADD CONSTRAINT test_assignments_assigned_to_user_fkey FOREIGN KEY (assigned_to_user) REFERENCES public.users(id);


--
-- TOC entry 4997 (class 2606 OID 33775)
-- Name: test_assignments test_assignments_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_assignments
    ADD CONSTRAINT test_assignments_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.tests(id) ON DELETE CASCADE;


--
-- TOC entry 4998 (class 2606 OID 33780)
-- Name: test_drafts test_drafts_original_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drafts
    ADD CONSTRAINT test_drafts_original_test_id_fkey FOREIGN KEY (original_test_id) REFERENCES public.tests(id) ON DELETE SET NULL;


--
-- TOC entry 4999 (class 2606 OID 33785)
-- Name: test_drafts test_drafts_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drafts
    ADD CONSTRAINT test_drafts_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 5000 (class 2606 OID 33790)
-- Name: test_results test_results_parameter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_results
    ADD CONSTRAINT test_results_parameter_id_fkey FOREIGN KEY (parameter_id) REFERENCES public.parameters(id);


--
-- TOC entry 5001 (class 2606 OID 33795)
-- Name: test_results test_results_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_results
    ADD CONSTRAINT test_results_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.test_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 5002 (class 2606 OID 33800)
-- Name: test_sessions test_sessions_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_sessions
    ADD CONSTRAINT test_sessions_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.tests(id);


--
-- TOC entry 5003 (class 2606 OID 33805)
-- Name: test_sessions test_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_sessions
    ADD CONSTRAINT test_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- TOC entry 5004 (class 2606 OID 33810)
-- Name: tests tests_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT tests_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- TOC entry 5005 (class 2606 OID 33815)
-- Name: user_answers user_answers_answer_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_answers
    ADD CONSTRAINT user_answers_answer_option_id_fkey FOREIGN KEY (answer_option_id) REFERENCES public.answer_options(id);


--
-- TOC entry 5006 (class 2606 OID 33820)
-- Name: user_answers user_answers_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_answers
    ADD CONSTRAINT user_answers_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.questions(id);


--
-- TOC entry 5007 (class 2606 OID 33825)
-- Name: user_answers user_answers_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_answers
    ADD CONSTRAINT user_answers_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.test_sessions(id) ON DELETE CASCADE;


--
-- TOC entry 5008 (class 2606 OID 33830)
-- Name: user_groups user_groups_group_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT user_groups_group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- TOC entry 5009 (class 2606 OID 33835)
-- Name: user_groups user_groups_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT user_groups_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


-- Completed on 2026-05-27 23:59:21

--
-- PostgreSQL database dump complete
--

\unrestrict AuI4rSzOQNCcZYSKDGfeJTbrDKWQlerFFlkQ0zKcfMyjr9as6wQq3cB0mXIfaMO

