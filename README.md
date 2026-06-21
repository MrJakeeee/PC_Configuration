# PC Configuration

Минималистичный сайт-конфигуратор ПК на Spring Boot, Thymeleaf, Spring Security, Spring Data JPA и PostgreSQL.

## Что реализовано

- регистрация и вход пользователей;
- роли `USER`, `ADMIN`, `DELIVERER`;
- каталог комплектующих с фильтрацией по категории и поиском;
- каталог готовых ПК;
- конфигуратор ПК с сохранением сборок;
- избранные сборки;
- оформление заказов по сборке или готовому ПК;
- отслеживание статуса заказа пользователем;
- профиль пользователя;
- админ-панель для категорий, комплектующих, готовых ПК, акций, заказов и платежей;
- панель доставщика с заказами, сроками и статусами доставки;
- стартовые демо-данные.

## PostgreSQL

По умолчанию приложение подключается к базе из проекта IntelliJ:

```properties
jdbc:postgresql://localhost:5432/Conf
username=postgres
password=postgres
```

Если пароль другой, перед запуском задайте переменную окружения:

```powershell
$env:DB_PASSWORD="ваш_пароль"
```

Или измените `src/main/resources/application.properties`.

## Запуск

1. Откройте проект в IntelliJ IDEA как Maven-проект через `pom.xml`.
2. Дождитесь загрузки зависимостей.
3. Убедитесь, что PostgreSQL запущен и база `Conf` создана.
4. Запустите класс `kg.kstu.pcconfiguration.PcConfigurationApplication`.
5. Откройте `http://localhost:8080`.

Тестовые аккаунты создаются автоматически:

- `admin / admin`
- `deliverer / deliverer`
- `user / user`
