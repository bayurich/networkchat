# "Сетевой чат"

## Описание проекта

обмена текстовыми сообщениями по сети с помощью консоли (терминала) между двумя и более пользователями.

**Первое приложение - сервер чата** - ожидает подключения пользователей.

**Второе приложение - клиент чата** - подключается к серверу чата и осуществляет доставку и получение новых сообщений.

### Запуск

1. Запустить сервер: server/src/main/java/MainServer

порт настраивается в параметре конфигурации **server.port**

2. Запустить клиент: client/src/main/java/MainClient

Для проверки многопользовательского режима запустить дубликат клиента: client/src/main/java/MainClient1 (при необходимости создайте нужное количество копий MainClient и запустите)

хост и порт настраиваются в параметрах конфигурации **server.host** и **server.port**

Логирование сессии производится в файл, указанный в параметре конфигурации **log.path**


