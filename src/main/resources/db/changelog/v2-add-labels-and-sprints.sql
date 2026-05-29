-- Создание таблицы labels
CREATE TABLE IF NOT EXISTS labels (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20),
    description VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- Создание таблицы task_labels
CREATE TABLE IF NOT EXISTS task_labels (
                                           task_id BIGINT NOT NULL,
                                           label_id BIGINT NOT NULL,
                                           PRIMARY KEY (task_id, label_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
    );

-- Создание таблицы sprints (если ещё нет)
CREATE TABLE IF NOT EXISTS sprints (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(200) NOT NULL,
    goal VARCHAR(500),
    start_date DATE,
    end_date DATE,
    velocity INTEGER,
    status VARCHAR(20) DEFAULT 'PLANNED',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    project_id BIGINT,
    FOREIGN KEY (project_id) REFERENCES projects(id)
    );

-- Добавление колонки sprint_id в tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS sprint_id BIGINT;
ALTER TABLE tasks ADD FOREIGN KEY (sprint_id) REFERENCES sprints(id);