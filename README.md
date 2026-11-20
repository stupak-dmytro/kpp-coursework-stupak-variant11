# Інформаційна система поповнення складських запасів

Курсова робота з дисципліни «Кросплатформене програмування», **варіант №11**.

- Студент: Ступак Дмитро Сергійович, група д2ТН
- Керівник: Заїка Світлана Олександрівна
- Кафедра комп'ютерних та інформаційних технологій і систем, Національний університет «Полтавська політехніка імені Юрія Кондратюка»

## Опис

Клієнт–серверна система обліку складських запасів і поповнення товарів. Користувач веде товари, постачальників і залишки, створює замовлення поповнення та проводить їх через чотири стани життєвого циклу. Для кожного замовлення сервер розраховує інтегральний показник пріоритетності.

Схема взаємодії:

```
JavaFX → HttpWarehouseClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL
```

## Предметна область

| Сутність | Призначення |
|---|---|
| Product (Товар) | Позиція номенклатури складу |
| Supplier (Постачальник) | Організація, яка виконує замовлення поповнення |
| Stock (Запас) | Залишок товару на дату, мінімальний рівень і споживання за добу |
| ReplenishmentOrder (Замовлення поповнення) | Заявка постачальнику; об'єкт життєвого циклу |

**Життєвий цикл замовлення:** `CREATED → APPROVED → ORDERED → RECEIVED` (СТВОРЕНО → СХВАЛЕНО → ЗАМОВЛЕНО → ОТРИМАНО). Дозволені лише послідовні переходи вперед; перевірку виконує Service-рівень сервера.

**Інтегральний показник:** `I = 0,40·F1 + 0,35·F2 + 0,25·F3`, усі фактори нормуються до діапазону 0–100:

- F1 — дефіцит запасу: `(minLevel − quantity) / minLevel · 100`, обмежений 0..100;
- F2 — інтенсивність споживання: `dailyConsumption / minLevel · 100`, обмежена 100;
- F3 — ризик затримки постачання: `0,5·(100 − reliability) + 0,5·min(100; leadTimeDays / 14 · 100)`.

Значення F1, F2, F3 та I рахує сервер (клас `RiskCalculator`), клієнт їх не вводить.

## Вимоги до середовища

- JDK 21
- Apache Maven (перевірено на 3.10.0)
- MySQL (наприклад, XAMPP) і phpMyAdmin
- Apache Tomcat **9.x** (використовується `javax.servlet`, Tomcat 10+ не підходить)
- JavaFX підключається через Maven, окремо встановлювати не потрібно

## Структура проєкту

```
kpp-coursework-stupak-variant11/
├── README.md
├── .gitignore
├── database.sql                 # створення БД, таблиць і демонстраційних даних
├── server/                      # Maven-проєкт (war): Servlet, Service, DAO, JDBC
│   ├── pom.xml
│   └── src/
│       ├── main/java/ua/edu/nupp/kpp/server/
│       ├── main/webapp/WEB-INF/web.xml
│       └── test/java/ua/edu/nupp/kpp/server/
└── client/                      # Maven-проєкт: JavaFX, HTTP-клієнт, CSV-імпорт
    ├── pom.xml
    ├── sample-data/
    │   ├── stock.csv            # коректні залишки для імпорту
    │   └── stock_with_errors.csv# приклад файлу з помилковими рядками
    └── src/
        ├── main/java/ua/edu/nupp/kpp/client/
        └── test/java/ua/edu/nupp/kpp/client/
```

## Запуск

### 1. База даних

1. Запустити MySQL (у XAMPP: модуль MySQL).
2. У phpMyAdmin імпортувати `database.sql` з кореня проєкту. Скрипт створює базу `warehouse_replenishment`, чотири таблиці та початкові дані (3 товари, 2 постачальники, 3 записи запасу). Його можна запускати повторно: база створюється заново.

Параметри підключення (`localhost:3306`, користувач `root`, порожній пароль: налаштування XAMPP за замовчуванням) задано в `server/src/main/java/ua/edu/nupp/kpp/server/DBUtil.java`. Якщо у вашому MySQL інші параметри, змініть їх там.

### 2. Сервер (Tomcat)

```
cd server
mvn clean package
```

1. Скопіювати `server/target/warehouse-replenishment-server.war` у каталог `webapps` встановленого Tomcat 9.x (попередньо видалити старий WAR і розгорнуту папку `warehouse-replenishment-server`, якщо вони є).
2. Запустити Tomcat (`bin/catalina.bat run` у Windows).
3. Перевірити: `http://localhost:8080/warehouse-replenishment-server/products` має повернути JSON-масив товарів.

### 3. Клієнт (JavaFX)

Перед запуском мають працювати MySQL і Tomcat.

```
cd client
mvn javafx:run
```

Команду потрібно запускати саме з каталогу `client`: шлях до CSV-файлів (`sample-data/...`) задається відносно нього.

## HTTP-маршрути

Базова адреса: `http://localhost:8080/warehouse-replenishment-server`

| URL | Servlet | Операції |
|---|---|---|
| `/products` | ProductServlet | GET: список; POST `action=add`, `update`, `delete` |
| `/suppliers` | SupplierServlet | GET: список; POST `action=add`, `update`, `delete` |
| `/stocks` | StockServlet | GET: список; POST `action=add`, `update`, `delete` |
| `/orders` | ReplenishmentOrderServlet | GET: список; POST `action=add`, `update`, `transition`, `delete` |

Параметри POST передаються як `application/x-www-form-urlencoded`; видалення виконується через `POST action=delete&id=...`. Відповідь: JSON. Помилка: код 400 (некоректні дані, недопустимий перехід тощо) або 500 (непередбачена помилка) і тіло `{"error": "..."}`.

## CSV-імпорт залишків

У вкладці «Запаси» кнопка «Імпортувати CSV» читає файл за відносним шляхом (за замовчуванням `sample-data/stock.csv`) у фоновому потоці (JavaFX `Task`), показує хід імпорту в `ProgressBar` і не блокує інтерфейс.

Формат (роздільник `;`, перший рядок — заголовок, десяткова крапка або кома):

```
product_id;quantity;min_level;daily_consumption
1;85.0;150.0;40.0
```

Правила для некоректних рядків: рядок пропускається, імпорт триває; наприкінці показується підсумок «Імпортовано N, пропущено M» і причини. Некоректним вважається рядок із хибною кількістю стовпців, нечисловим значенням або таким, який відхилив сервер (наприклад, неіснуючий товар, `min_level ≤ 0`, від'ємний залишок). Файл `sample-data/stock_with_errors.csv` призначено для перевірки цих правил. Некоректний заголовок або відсутній файл зупиняють імпорт із повідомленням про помилку.

## Перевірка

Тести JUnit 5 не потребують MySQL, Tomcat чи мережі.

```
cd server
mvn clean test       # очікується: Tests run: 38, Failures: 0, Errors: 0
mvn clean package    # збирає WAR разом з тестами

cd ../client
mvn clean test       # очікується: Tests run: 9, Failures: 0, Errors: 0
```

Додатково: після запуску Tomcat перевірити `GET /products`, `/suppliers`, `/stocks`, `/orders`, а в клієнті пройти сценарій «додати → змінити → видалити», створення замовлення (значення F1, F2, F3 та I розраховує сервер) і зміну стану.
