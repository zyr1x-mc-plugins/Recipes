# Recipes

A Paper plugin that adds a custom crafting system with unique items via PersistentDataContainer.

## Modules

| Module   | Path     | Purpose                                                          |
|----------|----------|------------------------------------------------------------------|
| Recipes  | root     | The main plugin (JAR: `build/dist/Recipes.jar`)                  |
| Api      | `Api/`   | Public API contract for third-party plugins (JAR: `build/dist/Recipes-api.jar`) |

## Build

```bash
./gradlew build
```

After a successful build the following artifacts are produced automatically:

```text
build/dist/
├── Recipes.jar      # plugin (already bundles the API classes)
└── Recipes-api.jar  # standalone API for developers
```

---

# API

## 1. Подключение API (Gradle)

The API is not (yet) published to a public Maven repository. Two options:

**Option A — project dependency (monorepo):**

```kotlin
dependencies {
    compileOnly(project(":Api"))
}
```

**Option B — local JAR:**

1. Build the project: `./gradlew build`
2. Add `build/dist/Recipes-api.jar` to your plugin's classpath as `compileOnly`.

## 2. Получение API

```kotlin
val api: RecipesApi? = RecipesApi.get()

if (api != null) {
    // работа с API
}
```

Or via Bukkit ServicesManager:

```kotlin
val api: RecipesApi? = Bukkit.getServicesManager().load(RecipesApi::class.java)
```

## 3. Ограничения по потокам

Все методы API и события вызываются/обрабатываются на **main server thread**.
Не вызывайте их из фоновых потоков.
