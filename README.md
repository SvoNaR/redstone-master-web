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
| http://localhost:8080/moderation | Модерация (роль MODERATOR / ADMIN) |
| http://localhost:8080/moderation/pseudo-video | Конвертер видео → псевдо-видео для мода |
| http://localhost:8080/admin/lesson-submissions | Очередь уроков на проверку (ADMIN) |

Параметр `?lang=en` переключает язык (по умолчанию `ru`).

## Псевдо-видео для мода (модераторы)

Мод воспроизводит **последовательность PNG-кадров** (15 fps, 854×480 по умолчанию), а не обычное видео.
В разделе **Модерация → Конвертер псевдо-видео** можно:

1. Задать `video id` (имя папки в ресурсах мода).
2. Загрузить исходное видео (mp4, webm и т.д.) — сервер вызывает **FFmpeg** и создаёт `frame_00000.png`, …, `meta.json`.
3. Скачать **JAR** для папки `mods` Minecraft или **ZIP** с кадрами.

Для полного урока с текстом и привязкой к каталогу обучения используйте **Редактор урока**, затем соберите JAR урока и отправьте администратору.

Настройки конвертации в `application.properties`:

```properties
app.moderation.ffmpeg-executable=ffmpeg
app.moderation.video-fps=15
app.moderation.video-width=854
app.moderation.video-height=480
```

На Windows укажите полный путь к `ffmpeg.exe`, если он не в `PATH`.

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
