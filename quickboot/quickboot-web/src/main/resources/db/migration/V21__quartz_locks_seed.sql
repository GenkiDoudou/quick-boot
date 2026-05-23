-- Quartz JDBC JobStore 必需的锁行（sched_name 与 ScheduleConfig 中 instanceName 一致）

DELETE FROM QRTZ_LOCKS WHERE sched_name = 'QuickScheduler';

INSERT INTO QRTZ_LOCKS (sched_name, lock_name) VALUES ('QuickScheduler', 'STATE_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name) VALUES ('QuickScheduler', 'TRIGGER_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name) VALUES ('QuickScheduler', 'JOB_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name) VALUES ('QuickScheduler', 'CALENDAR_ACCESS');
INSERT INTO QRTZ_LOCKS (sched_name, lock_name) VALUES ('QuickScheduler', 'MISFIRE_ACCESS');
