import sqlite3
import time

DB_FILENAME = "dashboard_log.db"

CREATE_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS logs (
  sequence_id INTEGER PRIMARY KEY AUTOINCREMENT,
  table_name TEXT,
  key TEXT,
  wall_time_ms INTEGER,
  value_type TEXT,
  value TEXT
)
"""

INSERT_LOG_POINT_SQL = """
INSERT INTO logs (table_name, key, wall_time_ms, value_type, value)
VALUES (?, ?, ?, ?, ?)
"""

SELECT_OLDEST_SEQUENCE_ID_SINCE_SQL = """
SELECT MAX(sequence_id)
FROM logs
WHERE wall_time_ms < ? AND table_name = ? AND key = ?
"""

SELECT_LOG_POINTS_SQL = """
SELECT sequence_id, wall_time_ms, value
FROM logs
WHERE sequence_id > ? AND table_name = ? AND key = ?
ORDER BY sequence_id ASC
"""

TYPE_NAME_BOOL = "bool"
TYPE_NAME_NUMBER = "number"
TYPE_NAME_STRING = "string"

class LogPoint:
    None

class RecorderDb:
    def __init__(self, ensureTable=False):
        self._connection = sqlite3.connect(DB_FILENAME, check_same_thread=True)
        if ensureTable:
            cursor = self._connection.cursor()
            cursor.execute(CREATE_TABLE_SQL)
            self._connection.commit()

    def addLogPoint(self, tableName, key, value):
        wallTimeMs = int(time.time() * 1000)
        if isinstance(value, bool):
            valueType = TYPE_NAME_BOOL
        elif isinstance(value, (float, int)):
            valueType = TYPE_NAME_NUMBER
        else:
            valueType = TYPE_NAME_STRING
        self._connection.cursor().execute(
            INSERT_LOG_POINT_SQL,
            (tableName, key, wallTimeMs, valueType, str(value)))
        self._connection.commit()

    def getStartSequenceIdForTime(self, startTimeMs, tableName, key):
        cursor = self._connection.cursor()
        cursor.execute(
            SELECT_OLDEST_SEQUENCE_ID_SINCE_SQL,
            (startTimeMs, tableName, key))
        return int(cursor.fetchone()[0]) or 0

    def genNumberLogPoints(self, prevSequenceId, tableName, key):
        cursor = self._connection.cursor()
        cursor.execute(SELECT_LOG_POINTS_SQL, (prevSequenceId, tableName, key))
        for row in cursor:
            logPoint = LogPoint()
            # TODO: I have no idea how the fuck this happens sometimes
            if len(row) < 3:
                continue
            logPoint.sequenceId = int(row[0])
            logPoint.wallTimeMs = int(row[1])
            logPoint.value = row[2]
            yield logPoint
