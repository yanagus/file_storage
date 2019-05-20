CREATE TABLE IF NOT EXISTS file (
    id              INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT 'Уникальный идентификатор файла',
    version         INTEGER NOT NULL,
    file_name       VARCHAR(200) COMMENT 'Название файла',
    original_name   VARCHAR(200) COMMENT 'Оригинальное название файла',
    download_count  INTEGER COMMENT 'Количество скачиваний'
)
COMMENT 'Файл';
