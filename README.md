# TaskFlow - Task Management System

## Описание
Система управления задачами и проектами с ролевой моделью.

## Технологии
- Java 21
- Spring Boot 3.2.5
- Spring Security + JWT
- PostgreSQL
- Liquibase
- Maven

## Функциональность
- CRUD операции для задач, проектов, пользователей
- Аутентификация и авторизация (JWT)
- Роли: ADMIN, MANAGER, USER
- Уведомления
- История изменений
- Комментарии и вложения
- Спринты и метки

## Запуск проекта

### Требования
- Java 21+
- Maven 3.9+
- PostgreSQL

### Настройка базы данных
1. Установите PostgreSQL
2. Создайте базу данных:
```sql
CREATE DATABASE taskflow;
```

### Локальный запуск
```bash
mvn spring-boot:run
```

### API Документация
- http://localhost:8080/swagger-ui/index.html

### Тестовый администратор
- Username: admin1
- Password: admin123

### Рекомендуемый порядок тестирования:
1. Авторизуйтесь через кнопку Authorize
2. Создайте проект - POST /api/projects
3. Создайте задачу - POST /api/tasks
4. Получите список задач - GET /api/tasks
5. Обновите статус задачи - PUT /api/tasks/{id}
6. Добавьте комментарий - POST /api/comments/task/{taskId}
7. Посмотрите комментарии - GET /api/comments/task/{taskId}
8. Создайте спринт - POST /api/sprints
9. Запустите спринт - POST /api/sprints/{id}/start
10. Проверьте уведомления - GET /api/notifications/unread