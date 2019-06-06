CREATE TABLE IF NOT EXISTS file (
    id              INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT 'Уникальный идентификатор файла',
    version         INTEGER NOT NULL,
    file_name       VARCHAR(200) COMMENT 'Название файла',
    original_name   VARCHAR(200) COMMENT 'Оригинальное название файла',
    download_count  INTEGER COMMENT 'Количество скачиваний'
)
COMMENT 'Файл';

CREATE TABLE IF NOT EXISTS user (
    id                  INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT 'Уникальный идентификатор пользователя',
    version             INTEGER NOT NULL,
    user_name           VARCHAR(50) NOT NULL UNIQUE COMMENT 'Имя пользователя',
    password            VARCHAR(50) NOT NULL COMMENT 'Пароль пользователя',
    email               VARCHAR(50) NOT NULL COMMENT 'E-mail',
    code                VARCHAR(100) COMMENT 'Код регистрации',
    registration_date   DATETIME COMMENT 'Дата регистрации',
    is_confirmed        BOOLEAN COMMENT 'Статус учетной записи (подтверждена или нет)'
)
COMMENT 'Пользователь';

CREATE TABLE IF NOT EXISTS file (
    id              INTEGER PRIMARY KEY AUTO_INCREMENT COMMENT 'Уникальный идентификатор файла',
    version         INTEGER NOT NULL,
    file_name       VARCHAR(200) COMMENT 'Название файла',
    original_name   VARCHAR(200) COMMENT 'Оригинальное название файла',
    download_count  INTEGER COMMENT 'Количество скачиваний',
    user_id         INTEGER COMMENT 'Уникальный идентификатор пользователя, внешний ключ',
    FOREIGN KEY (user_id) REFERENCES User (id) ON DELETE CASCADE ON UPDATE CASCADE
)
COMMENT 'Файл';

CREATE TABLE IF NOT EXISTS access (
	user_id             INTEGER NOT NULL COMMENT 'Уникальный идентификатор пользователя, внешний ключ',
    subscriber_id       INTEGER NOT NULL COMMENT 'Уникальный идентификатор пользователя, запрашивающего доступ к файлу, внешний ключ',
    version             INTEGER NOT NULL,
    read_access         BOOLEAN COMMENT 'Доступ на чтение',
    read_request        BOOLEAN COMMENT 'Запрос на чтение',
    download_access     BOOLEAN COMMENT 'Доступ на скачивание',
    download_request    BOOLEAN COMMENT 'Запрос на скачивание',
    FOREIGN KEY (user_id) REFERENCES User (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (subscriber_id) REFERENCES User (id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (user_id, subscriber_id)
)
COMMENT 'Доступ к файлам для других пользователей';

CREATE INDEX IX_User_Code ON User (code);
