CREATE TABLE IF NOT EXISTS lectio_calendar_events (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        title TEXT,
                                        team TEXT,
                                        teachers TEXT,
                                        room TEXT,
                                        state TEXT,
                                        queried_week_num INTEGER,
                                        start DATETIME NOT NULL,
                                        end DATETIME NOT NULL,
                                        created_at DATETIME NOT NULL
);