-- Baseline：占位表，验证 Flyway 在 H2(dev) 与 MySQL(prod) 下均可执行。
-- 后续业务表请追加 V2__*.sql、V3__*.sql …

CREATE TABLE IF NOT EXISTS qc_flyway_baseline (
    id BIGINT NOT NULL PRIMARY KEY
);
