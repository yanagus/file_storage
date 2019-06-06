# Многопользовательский сервис хранения файлов</br>
Java 1.8, Spring Boot 2.1.3.RELEASE, MySQL, FreeMarker, Bootstrap.</br>
Пример обращения http://localhost:8080</br>

Главная страница:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/main.png)

Форма авторизации:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/login.png)
Неудачная авторизация:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/unauthenticated.png)
Форма регистрации нового пользователя:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/reg_form.png)
Форма регистрации нового пользователя, если данные не введены:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/reg_form2.png)
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/reg_form3.png)
если пользователь с таким именем существует:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/reg_form4.png)
При успешной регистрации выдается сообщение, что необходимо перейти по ссылке отправленной на email.
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/activate.png)
Ссылка действительна в течение 24х часов. По прошествии большего времени с момента регистрации высылается новая ссылка
и выдается сообщение:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/link_expired.png)
При успешной активации выдается форма логина с сообщением, что учетная запись активирована
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/successfully_activated.png)

Пользователь может загружать и скачивать загруженные файлы. Удалять можно только свои файлы.</br>
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/my_files.png)
Пользователь может запросить доступ на просмотр списка или на скачивание файлов другого пользователя.
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/no_access.png)
Пользователь может дать доступ другому пользователю сервиса на просмотр списка своих файлов или на скачивание.</br>
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/subscribers.png)
Страница, если разрешён только просмотр файлов:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/read.png)
Страница, если разрешено скачивание:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/download.png)

Выход из системы:
![Image alt](https://github.com/yanagus/file_storage/raw/master/image/logout.png)