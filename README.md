# MAX Bot Spring Boot Starter

[![Java](https://img.shields.io/badge/Java-17+-blue)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)

`MAX Bot Spring Boot Starter` - это Spring Boot стартер для разработки ботов для [MAX](https://max.ru).

Стартер предоставляет:

- аннотационную маршрутизацию обработчиков: `@CommandRequest`, `@MessageRequest`, `@CallbackRequest`, `@BotStartedRequest` и другие
- проверку прав доступа в обработчиках через `@PreAuthorize`
- поддержку сценариев с состояниями через `StateStore`
- прием обновлений через `long polling` и `webhook`
- перехватчики и централизованную обработку ошибок через `HandlerExceptionResolver`
- интеграцию с Spring Boot Actuator и Micrometer
- вспомогательные методы для загрузки файлов

## Быстрый старт

Минимальная конфигурация:

```yaml
max:
  bot:
    access-token: ${MAX_BOT_TOKEN}
```

Пример простого бота:

```java
import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.BotStartedRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;

@MaxController
public class MyController {

    @BotStartedRequest
    public void onStart(UpdateContext ctx) {
        ctx.reply("Привет! Я бот для MAX.");
    }

    @CommandRequest("help")
    public void help(UpdateContext ctx) {
        ctx.reply("Доступные команды:\n/help");
    }

    @MessageRequest(textRegex = ".*")
    public void echo(UpdateContext ctx) {
        ctx.reply("Вы написали: " + ctx.text());
    }
}
```

## Аннотации

| Аннотация | Назначение | Параметры |
|---|---|---|
| `@MaxController` | Помечает класс с обработчиками | - |
| `@CommandRequest("cmd")` | Обрабатывает команду `/cmd` | `value`, `state`, `order` |
| `@MessageRequest(textRegex)` | Обрабатывает текст по регулярному выражению | `textRegex`, `state`, `order` |
| `@CallbackRequest(prefix)` | Обрабатывает callback по префиксу данных | `prefix`, `state`, `order` |
| `@BotStartedRequest` | Срабатывает при нажатии кнопки Start | `order` |
| `@BotAddedRequest` | Бот добавлен в чат | `order` |
| `@BotRemovedRequest` | Бот удален из чата | `order` |
| `@UserAddedRequest` | Пользователь добавлен в чат | `order` |
| `@UserRemovedRequest` | Пользователь удален из чата | `order` |
| `@MessageEditedRequest` | Сообщение изменено | `order` |
| `@MessageRemovedRequest` | Сообщение удалено | `order` |
| `@ChatTitleChangedRequest` | Изменено название чата | `order` |
| `@MaxBotExceptionHandler(ExceptionType.class)` | Локальная или глобальная обработка исключений | `value` |
| `@MaxBotControllerAdvice` | Глобальные обработчики исключений | - |

Методы-обработчики должны возвращать `void`.

Поддерживаемые аргументы методов:

- `UpdateContext`
- `Update`
- `MaxApi`
- `ru.maxbot.core.model.User`
- `org.springframework.security.core.Authentication`

Если в приложении подключен Spring Security, объект `Authentication` можно получать напрямую в сигнатуре обработчика или обработчика исключений, без обращения к `SecurityContextHolder`.

Обработчики с меньшим значением `order` выполняются раньше.

## UpdateContext

`UpdateContext` содержит данные текущего обновления и вспомогательные методы для ответа пользователю.

```java
// Данные обновления
ctx.chatId();
ctx.text();
ctx.sender();           // ru.maxbot.core.model.User
ctx.callbackData();
ctx.callbackId();
ctx.messageId();
ctx.payload();
ctx.update();
ctx.api();

// Ответы
ctx.reply("текст");
ctx.reply(message);
ctx.editMessage("новый текст");
ctx.deleteMessage();
ctx.answerCallback("ок");
ctx.answerCallbackWithMessage(message);

// Работа с чатом
ctx.getChat();          // ru.maxbot.core.model.Chat
ctx.getChatMembers();   // List<ru.maxbot.core.model.ChatMember>
ctx.leaveChat();

// Загрузка файлов
ctx.uploadImage(file);
ctx.uploadVideo(file);
ctx.uploadAudio(file);
ctx.uploadFile(file);
ctx.replyWithImage("Фото", file);

// Состояния
ctx.state();
ctx.setState("STEP_2");
ctx.clearState();
```

## Построение сообщений

`OutgoingMessage` - это удобная обертка над моделями запросов MAX API.

```java
import java.util.List;
import ru.maxbot.core.outgoing.OutgoingMessage;

var keyboard = List.of(
    List.of(
        OutgoingMessage.callbackButton("Да", "confirm:yes"),
        OutgoingMessage.callbackButton("Нет", "confirm:no")
    ),
    List.of(
        OutgoingMessage.linkButton("Документация", "https://dev.max.ru")
    )
);

ctx.reply(OutgoingMessage.text("Подтвердить действие:")
    .keyboard(keyboard)
    .build());
```

### Вложения

```java
String imageToken = ctx.uploadImage(new File("photo.jpg"));

ctx.reply(OutgoingMessage.text("Фотография:")
    .attach(OutgoingMessage.photo(imageToken))
    .build());

ctx.reply(OutgoingMessage.text("Файлы:")
    .attach(OutgoingMessage.file(fileToken1))
    .attach(OutgoingMessage.file(fileToken2))
    .build());
```

Поддерживаемые помощники:

- `OutgoingMessage.photo(token)`
- `OutgoingMessage.video(token)`
- `OutgoingMessage.audio(token)`
- `OutgoingMessage.file(token)`

## Сценарии с состояниями

Пример простого пошагового сценария:

```java
@MaxController
public class OrderBot {

    @CommandRequest("order")
    public void startOrder(UpdateContext ctx) {
        ctx.reply("Введите название товара:");
        ctx.setState("WAITING_PRODUCT");
    }

    @MessageRequest(textRegex = ".*", state = "WAITING_PRODUCT")
    public void receiveProduct(UpdateContext ctx) {
        ctx.reply("Товар: " + ctx.text() + "\nВведите количество:");
        ctx.setState("WAITING_QUANTITY");
    }

    @MessageRequest(textRegex = "\\d+", state = "WAITING_QUANTITY")
    public void receiveQuantity(UpdateContext ctx) {
        ctx.reply("Заказ оформлен. Количество: " + ctx.text());
        ctx.clearState();
    }
}
```

Обработчики без параметра `state` работают в любом состоянии.

По умолчанию используется `InMemoryStateStore`. Если нужно постоянное хранение состояний, зарегистрируйте собственный бин `StateStore`.

## Несколько классов `@MaxController`

Крупного бота удобно делить по зонам ответственности. Все обработчики из всех `@MaxController` объединяются в единый диспетчер.

```java
@MaxController
public class MainMenuBot {

    @CommandRequest(value = "start", order = -100)
    public void start(UpdateContext ctx) {
    }
}

@MaxController
public class OrderBot {

    private final OrderService orderService;

    public OrderBot(OrderService orderService) {
        this.orderService = orderService;
    }

    @CommandRequest("order")
    public void startOrder(UpdateContext ctx) {
    }
}
```

## Перехватчики и обработка ошибок

```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(UpdateContext ctx) {
        log.info("Update type={}, chatId={}", ctx.update().type(), ctx.chatId());
        return true;
    }

    @Override
    public void postHandle(UpdateContext ctx) {
        log.info("Handler completed for chatId={}", ctx.chatId());
    }

    @Override
    public void afterCompletion(UpdateContext ctx, Exception ex) {
        if (ex != null) {
            log.error("Handler failed for chatId={}", ctx.chatId(), ex);
        }
    }
}

@Bean
public HandlerExceptionResolver handlerExceptionResolver() {
    return (ctx, handler, ex) -> {
        ctx.reply("Что-то пошло не так. Попробуйте позже.");
        return true;
    };
}
```

Также доступны контроллерные обработчики исключений:

```java
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;

@MaxController
public class OrderController {

    @CommandRequest("order")
    public void order() {
        throw new IllegalStateException("Не удалось оформить заказ");
    }

    @MaxBotExceptionHandler
    public void handleLocal(IllegalStateException ex, UpdateContext ctx) {
        ctx.reply("Локальная ошибка: " + ex.getMessage());
    }
}

@MaxBotControllerAdvice
public class GlobalBotAdvice {

    @MaxBotExceptionHandler(IllegalArgumentException.class)
    public void handleGlobal(UpdateContext ctx, IllegalArgumentException ex) {
        ctx.reply("Глобальная ошибка: " + ex.getMessage());
    }
}
```

## Spring Security

Если `spring-security-core` находится в classpath, можно использовать аннотации Spring Security на методах-обработчиках.

```java
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import ru.maxbot.starter.security.BotAuthenticationConverter;

@Bean
public BotAuthenticationConverter botAuthenticationConverter() {
    return context -> {
        var sender = context.sender();
        if (sender == null) {
            return new AnonymousAuthenticationToken(
                    "maxbot",
                    "anonymousUser",
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                sender,
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN"));
    };
}

@MaxController
public class AdminController {

    @PreAuthorize("hasRole('ADMIN')")
    @CommandRequest("admin")
    public void adminOnly(UpdateContext ctx, Authentication authentication) {
        ctx.reply("Команда администратора для " + authentication.getName());
    }
}

@MaxBotControllerAdvice
public class SecurityAdvice {

    @MaxBotExceptionHandler(AuthorizationDeniedException.class)
    public void handleDenied(UpdateContext ctx, AuthorizationDeniedException ex) {
        ctx.reply("Доступ запрещен");
    }
}
```

Замечания:

- `BotAuthenticationConverter` преобразует входящее обновление в объект `Authentication`
- `SecurityContextHolder` заполняется перед вызовом обработчика и очищается после завершения обработки
- `Authentication` может быть автоматически передан в параметры метода обработчика

## Webhook

```yaml
max:
  bot:
    access-token: ${MAX_BOT_TOKEN}
    webhook:
      enabled: true
      path: /webhook
      secret: your-secret-here
```

Если включен `webhook.enabled=true`, стартер регистрирует Spring MVC endpoint по указанному пути.

## Actuator

Если в classpath есть `spring-boot-starter-actuator`, стартер публикует информацию о состоянии:

```text
GET /actuator/health
```

## Метрики

Если в classpath есть `micrometer-core`, стартер собирает:

- `maxbot.updates.received`
- `maxbot.handler.duration`
- `maxbot.handler.errors`

## Повторные попытки

Операции загрузки файлов используют повторные попытки с экспоненциальной задержкой по умолчанию.

Собственная политика:

```java
@Bean
public RetryPolicy retryPolicy() {
    return new RetryPolicy(5, 1000, 2.0);
}
```

## Примеры

Модуль `examples/` содержит несколько демонстрационных приложений разного уровня сложности:

- `ru.maxbot.examples.minimal` - минимальный бот с `/help`, `/echo` и обработкой произвольного сообщения
- `ru.maxbot.examples.security` - пример ролевого доступа через `@PreAuthorize`
- `ru.maxbot.examples.fsm` - пример диалога с состояниями через `StateStore`
- `ru.maxbot.examples.pizza` - более цельный пример бота доставки пиццы с меню, оформлением заказа, профилем и служебными командами

Запуск примеров:

```bash
MAX_BOT_TOKEN=... mvn -pl examples spring-boot:run -Dspring-boot.run.mainClass=ru.maxbot.examples.minimal.MinimalExampleApplication
```

```bash
MAX_BOT_TOKEN=... mvn -pl examples spring-boot:run -Dspring-boot.run.mainClass=ru.maxbot.examples.security.SecurityExampleApplication
```

```bash
MAX_BOT_TOKEN=... mvn -pl examples spring-boot:run -Dspring-boot.run.mainClass=ru.maxbot.examples.fsm.FsmExampleApplication
```

```bash
MAX_BOT_TOKEN=... mvn -pl examples spring-boot:run -Dspring-boot.run.mainClass=ru.maxbot.examples.ExampleApplication
```

## Требования

- Java 17+
- Spring Boot 3.x
- токен бота MAX

## Структура проекта

```text
max-bot-spring-boot-starter/
|-- max-bot-core/
|-- max-bot-spring-boot-starter/
`-- examples/
```
