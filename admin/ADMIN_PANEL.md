# BandResearch Admin Panel

Це окрема веб-адмінка для Render. Вона підключається до тієї ж Supabase бази, що й Android-додаток, і дає:

- вхід за поштою та паролем адміністратора;
- статистику по користувачах, гуртах, треках, плейлістах, історії, заявках і сповіщеннях;
- перегляд, пошук, створення, редагування та видалення записів у таблицях додатку.

## Змінні середовища для Render

У Render треба додати:

- `SUPABASE_URL` - URL твого Supabase проєкту.
- `SUPABASE_SERVICE_ROLE_KEY` - service role key з Supabase. Не використовуй anon key для адмінки.
- `ADMIN_EMAIL` - пошта, з якою можна зайти в адмінку.
- `ADMIN_PASSWORD_HASH` - bcrypt-хеш пароля.
- `SESSION_SECRET` - Render згенерує автоматично через `render.yaml`.

Для швидкого тесту можна замість `ADMIN_PASSWORD_HASH` задати `ADMIN_PASSWORD`, але для продакшну краще саме хеш.

## Як зробити bcrypt-хеш

Після `npm install` можна виконати:

```bash
node -e "import bcrypt from 'bcryptjs'; console.log(await bcrypt.hash('твій_пароль', 12))"
```

Скопіюй результат у `ADMIN_PASSWORD_HASH` на Render.

## Локальний запуск

```bash
npm install
npm start
```

Після запуску сайт буде доступний на `http://localhost:3000`.
