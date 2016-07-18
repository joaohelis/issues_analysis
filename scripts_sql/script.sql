CREATE TEMP TABLE alteracao_de_responsabilidade AS
SELECT t.id_tarefa, COUNT(*) alteracao_de_responsabilidade FROM iproject.tarefa t, iproject.log_tarefa l
WHERE t.id_build_associada in (select id_build from iproject.build b
		     where b.id_sistema IN (2, 3, 4) and
		     b.data_publicacao is not null) 
      and t.inicio is not null and t.fim is not null
      and l.id_tarefa = t.id_tarefa and l.id_tipo_log = 20
GROUP BY t.id_tarefa

CREATE TEMP TABLE tarefa_commit_info AS
SELECT c.task_id id_tarefa, SUM(c.churn) churn, sum(c.javafiles) javafiles, count(*) commits 
FROM deliverydelay.commit c
WHERE c.task_id IS NOT NULL AND c.task_id <> -1
GROUP BY c.task_id

CREATE OR REPLACE FUNCTION has_stacktrace(descricao_tarefa varchar) RETURNS BOOLEAN AS $$
DECLARE
    descricao_tarefa ALIAS FOR $1;
BEGIN
    IF descricao_tarefa like '%Exception%' THEN
	RETURN TRUE;
    ELSE
	RETURN FALSE;
    END IF;
END;
$$ LANGUAGE plpgsql;


SELECT t.id_tarefa task_id, p.ordem priority, tarefa_commit.churn, tarefa_commit.javafiles, LENGTH(t.descricao) description_lenght, 
DATE_PART('day',  t.fim - t.inicio) fix_time, 
COALESCE(alt_resp.alteracao_de_responsabilidade, 0) developer_changes, s.denominacao component,
(SELECT COUNT(*) 
 FROM  iproject.tarefa sub_t
 INNER JOIN iproject.build sub_b ON sub_t.id_build_associada = sub_b.id_build
 WHERE sub_t.id_usuario = t.id_usuario AND
 sub_b.data_publicacao IS NOT NULL AND t.fim IS NOT NULL AND
 sub_b.data_publicacao < b.data_publicacao) reporter_experience, -- REPORTER EXPERIENCE SUBQUERY
COALESCE((SELECT AVG(DATE_PART('day', sub_b.data_publicacao - (sub_t.fim - sub_t.inicio))) 
 FROM  iproject.tarefa sub_t
 INNER JOIN iproject.build sub_b ON sub_t.id_build_associada = sub_b.id_build
 WHERE sub_t.id_usuario = t.id_usuario AND
 sub_b.data_publicacao IS NOT NULL AND t.fim IS NOT NULL AND
 sub_b.data_publicacao < b.data_publicacao), 0) reporter_delivery_delay, -- REPORTER DELIVERY DELAY SUBQUERY
(SELECT COUNT(*) FROM iproject.log_tarefa log_t WHERE log_t.id_tarefa = t.id_tarefa) issue_activities, -- ISSUE ACTIVITIES SUBQUERY
(SELECT COUNT(*) FROM iproject.tarefa sub_t
INNER JOIN iproject.build sub_b ON sub_t.id_build_associada = sub_b.id_build
LEFT JOIN iproject.subsistema sub_s ON sub_s.id_sub_sistema = sub_t.id_subsistema
WHERE sub_s.id_sistema = s.id_sistema -- IN (2, 3, 4) 
AND sub_t.fim <= t.fim AND sub_b.data_publicacao > t.fim) workload, -- PROJECT WORKLOAD
(SELECT COUNT(*) FROM iproject.tarefa sub_t
INNER JOIN iproject.build sub_b ON sub_t.id_build_associada = sub_b.id_build
LEFT JOIN iproject.subsistema sub_s ON sub_s.id_sub_sistema = sub_t.id_subsistema
WHERE sub_s.id_sistema = s.id_sistema -- IN (2, 3, 4) 
AND sub_t.fim <= t.fim AND sub_b.data_publicacao > t.fim AND sub_t.id_tarefa <= t.id_tarefa) backlog_position, -- BACKLOG POSITION
(SELECT has_stacktrace(t.descricao)) stacktrace,
s.id_sistema project,
 DATE_PART('day', b.data_publicacao - (t.fim - t.inicio)) delivery_delay -- DELIVERYDELAY
FROM iproject.tarefa t
INNER JOIN iproject.build b ON t.id_build_associada = b.id_build
LEFT JOIN iproject.prioridade p ON p.id_prioridade = t.id_prioridade
LEFT JOIN iproject.subsistema s ON s.id_sub_sistema = t.id_subsistema
LEFT JOIN alteracao_de_responsabilidade AS alt_resp ON alt_resp.id_tarefa = t.id_tarefa
LEFT JOIN tarefa_commit_info AS tarefa_commit ON tarefa_commit.id_tarefa = t.numtarefa
WHERE s.id_sistema IN (2, 3, 4) AND b.data_publicacao IS NOT NULL AND
      t.fim IS NOT NULL

-- id_sistema{
-- sigaa: 2,
-- sipac: 3,
-- sigrh: 4
-- }

-- create commit table

CREATE TABLE deliverydelay.commit
(
  revision character varying(50) NOT NULL, -- primary key
  commit_comment character varying(10000),
  createdAt timestamp with time zone,
  author character varying(50),
  churn integer,
  javaFiles integer,
  task_id integer NOT NULL,
  repository_type integer,
  CONSTRAINT commit_pkey PRIMARY KEY (revision),
  CONSTRAINT task_id_fkey FOREIGN KEY (task_id)
      REFERENCES iproject.tarefa (id_tarefa) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)

CREATE TABLE deliverydelay.commit
(
  revision character varying(50) NOT NULL, -- primary key
  commit_comment character varying(10000),
  createdAt timestamp with time zone,
  author character varying(50),
  churn integer,
  javaFiles integer,
  task_id integer,
  repository_type integer,
  id_sistema integer,
  CONSTRAINT commit_pkey PRIMARY KEY (revision),
  --CONSTRAINT task_id_fkey FOREIGN KEY (task_id)
  --    REFERENCES iproject.tarefa (id_tarefa) MATCH SIMPLE
  --    ON UPDATE NO ACTION ON DELETE NO ACTION
  CONSTRAINT id_sistema_fkey FOREIGN KEY (id_sistema)
      REFERENCES iproject.sistema (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)