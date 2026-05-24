# Redstone Master Web

Отдельное веб-приложение на **Spring Boot 3** (Java 21). Не входит в сборку Fabric-мода.

При сборке Maven копирует языковые файлы, каталог обучения и текстуры из мода
(`src/main/resources/assets/redstone-master/` в корне репозитория).

## Требования

- JDK 21
- Maven 3.9+

## Запуск

```bash
cd other_projects/redstone-master-web
mvn spring-boot:run
```

Windows:

```bat
other_projects\redstone-master-web\run.bat
```

## Страницы

| URL | Описание |
|-----|----------|
| http://localhost:8080/ | Главная: о моде + установка |
| http://localhost:8080/tutorial | Каталог обучения |
| http://localhost:8080/tutorial/{sectionId} | Раздел |
| http://localhost:8080/tutorial/{sectionId}/{lessonId} | Урок |
| http://localhost:8080/settings | Настройки и клавиши |

Параметр `?lang=en` переключает язык (по умолчанию `ru`).

## REST API

- `GET /api/info`
- `GET /api/tutorial/sections?lang=ru`
- `GET /api/settings?lang=ru`
- `GET /api/keys?lang=ru`

## Сборка

```bash
mvn clean package
java -jar target/redstone-master-web-1.0.0.jar
```

## Структура

```
src/main/java/ru/redstonemaster/web/
  controller/     — страницы и REST
  service/        — загрузка JSON мода, справочники
  model/          — DTO
  locale/         — ru / en
src/main/resources/
  templates/      — Thymeleaf
  static/css/     — стили
```
