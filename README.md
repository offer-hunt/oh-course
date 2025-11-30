# oh-course

Сервис для управления курсами в проекте AI Hunt

## 1. Локальный запуск

Для локальной разработки используется Docker.

### Запуск базы данных

1. Убедитесь, что у вас установлен [Docker](https://www.docker.com/).
2. Выполните команду в корневой папке проекта, чтобы запустить контейнер с базой данных PostgreSQL:

```bash
docker-compose up -d
```
После выполнения этой команды база данных будет доступна по адресу `localhost:5432`.

### Остановка базы данных
```bash
docker-compose down
```

## 2. Docker образ

После каждого успешного билда в ветке `main`, CI автоматически собирает и публикует Docker образ.

*   **Репозиторий на Docker Hub:** `https://hub.docker.com/r/termoler/oh-course`
*   **Имя образа:** `termoler/oh-course:latest`

## 3. Тестирование через Swagger

Соберите и запустите сервис под профилем local:

```bash
./gradlew clean build
java -jar build/libs/oh-course-0.0.1.jar --spring.profiles.active=local
```

Перейдите по ссылке:

**[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**