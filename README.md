При старте nginx в контейнере со следующей конфигурацией:

```yaml
services:
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
```

В файле `/etc/nginx/nginx.conf` содержится следующая конфигурация:

```nginx configuration
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log notice;
pid        /var/run/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    keepalive_timeout  65;

    #gzip  on;

    include /etc/nginx/conf.d/*.conf;
}
```

В этом файле сервер не настроен, он только подгружает `/etc/nginx/conf.d/*.conf`  
В папке `conf.d` есть только файл `default.conf`:  

```nginx configuration
server {
    listen       80;
    listen  [::]:80;
    server_name  localhost;

    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }

    #error_page  404              /404.html;

    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }

    # proxy the PHP scripts to Apache listening on 127.0.0.1:80
    #
    #location ~ \.php$ {
    #    proxy_pass   http://127.0.0.1;
    #}

    # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
    #
    #location ~ \.php$ {
    #    root           html;
    #    fastcgi_pass   127.0.0.1:9000;
    #    fastcgi_index  index.php;
    #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
    #    include        fastcgi_params;
    #}

    # deny access to .htaccess files, if Apache's document root
    # concurs with nginx's one
    #
    #location ~ /\.ht {
    #    deny  all;
    #}
}
```

В нем уже настроен сервер, который слушает порт 80, но proxy_pass не настроен  

> Добавлять в конфигурацию docker-compose строку  
> `./nginx/nginx.conf:/etc/nginx/vhosts-includes`  
> смысла нет, так как эта конфигурация не подгружается.  
> Я давал пример из моего nginx, который был сконфигурирован
> при покупке сервера.  
> В нем есть строки:
> ```nginx configuration
> server {
>   ...
>   include /etc/nginx/vhosts-includes/*.conf;
>   ...
> }
> ```
> Поэтому у меня конфигурация из папки `vhosts-includes` подгружаются


Можно создать файл `default.conf` и в compose прописать:

```yml
services:
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
```

Тогда вместо конфигурации как в начале, будет подгружен нужный файл.  
В нем я сделал:

```nginx configuration
server {
    listen       80;
    server_name  localhost;

    location /backend {
        proxy_pass http://localhost:8080/;
    }
}
```

Создал простейший бэк, который слушает localhost:8080 и отвечает на него  

Nginx проксирует запросы, но бэк не отвечает.  
Проблему вижу в следующем:  
nginx проксирует запрос на `http://127.0.0.1:8080/`, но что это такое внутри контейнера никто не знает.  

Если посмотреть адреса приложений подключенных к сети внутри контейнеров, то их два:
- nginxback-abc-1
- nginxback-nginx-1

Сделал проксирование на nginxback-abc-1:8080

```nginx configuration
server {
    listen       80;
    listen  [::]:80;
    server_name  localhost;

    location /backend {
        proxy_pass http://nginxback-abc-1:8080/;
    }
}
```

Вот теперь запросы проходят!

> Ты был прав, оказывается можно указывать имя в настройках nginx!!!  
> Но это должно быть URL, который присваивается внутренним DNS docker,  
> а не имя сервиса из compose файла















