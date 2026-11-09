# ifs25024-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan pribadi (Spring Boot, Maven, Vaadin) dengan Clean Architecture.
Data disimpan in-memory dan hilang saat aplikasi di-restart. Port dari `APP_PORT` di `.env` (default 8080).

Package: `org.delcom.ifs25024pbo2026springbootcashflow`

## Menjalankan

```
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
.\mvnw.cmd clean package -Pproduction
```

## Struktur

```
domain/entity      Transaction, TransactionType, SortOption
domain/exception   DomainException, EntityNotFoundException, ValidationException
domain/repository  IRepository<T, ID>, ITransactionRepository
usecase            FinanceUseCase
adapter/repository InMemoryRepository<T, ID>, TransactionRepository
adapter/presenter  FinancePresenter
framework/view     FinanceView (Vaadin)
config             AppConfig
```
