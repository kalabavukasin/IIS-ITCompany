# Sistem za upravljanje zapošljavanjem - PL/SQL implementacija

**Predlog projekta za predmet Sistemi baze podataka**

**Autor:** [Vaše ime i broj indeksa]

## 1. Uvod

### 1.1. Opšti opis sistema

Sistem za upravljanje zapošljavanjem je informacijski sistem koji omogućava kompanijama da digitalizuju i automatizuju procese zapošljavanja. Sistem integriše više poslovnih funkcija u jednu platformu, uključujući upravljanje oglasima za posao, praćenje prijava kandidata, vođenje intervjua, evaluaciju kandidata i upravljanje ponudama za zapošljavanje.

Sistem se sastoji od sledećih integrisanih podsistema:
- **Podsistem za upravljanje oglasima za posao** - kreiranje, ažuriranje i upravljanje oglasima za posao
- **Podsistem za praćenje prijava** - upravljanje prijavama kandidata i njihovim statusima
- **Podsistem za vođenje intervjua** - planiranje i praćenje intervjua sa kandidatima
- **Podsistem za evaluaciju i testiranje** - vođenje testova i evaluacija kandidata
- **Podsistem za upravljanje ponudama** - kreiranje i praćenje ponuda za zapošljavanje
- **Podsistem za analitiku i izveštavanje** - generisanje izveštaja o performansama procesa zapošljavanja

## 2. Predlog implementacije

U okviru projekta Informacijskih sistema, koristiće se PostgreSQL baza podataka. Stoga će se u ovom projektu koristiti PL/pgSQL.

### 2.1. PL/SQL тригери

**Tema:** Automatsko arhiviranje i praćenje istorije promena statusa aplikacija i ponuda

**Opis rada:**
Тригери се активирају при свакој промени статуса у процесу запошљавања. Конкретно:

- `AFTER UPDATE` на табели `applications` (за промене статуса пријаве)
- `AFTER UPDATE` на табели `offers` (за промене статуса понуде)
- `AFTER INSERT/UPDATE` на табели `application_status_history` (за праћење прелазака између фаза)

При свакој успешној промени, следећи подаци се снимају у табелу `audit_logs`:
- `user_id` (ID корисника који је извршио промену)
- `action` (тип акције: STATUS_CHANGE, OFFER_UPDATE, STAGE_TRANSITION)
- `entity_type` (тип ентитета: APPLICATION, OFFER, STATUS_HISTORY)
- `entity_id` (ID ентитета на коме је извршена промена)
- `before_data_json` (JSON са претходним стањем)
- `after_data_json` (JSON са новим стањем)
- `time_utc` (време промене)

Ако се више статуса мења у једној акцији, свака промена се снима као посебан унос у аудит лог, све означене истим `time_utc` и `user_id` да се покаже да припадају истој акцији.

### 2.2. PL/pgSQL функције

**Tema:** Funkcija za izračunavanje kompleksnih metrika performansi procesa zapošljavanja

**Opis rada:**
Ова функција има за циљ да прође кроз сву сложену логику процеса запошљавања и врати комплексне метрике за одређени временски период. Функција `calculate_recruitment_metrics` прима параметре:
- `start_date` - почетни датум периода
- `end_date` - крајњи датум периода

Функција враћа сложени тип који укључује:
- Укупан број пријава у периоду
- Број успешно запошљених кандидата
- Просечно време до запошљавања (у данима)
- Конверзионе стопе по фазама процеса
- Проценат одбијања понуда
- Однос позвани/одбијени кандидати
- Детекцију уских грла у процесу

Функција користи сложене SQL упите који спајају податке из табела `applications`, `application_status_history`, `offers`, `job_postings` и `users`.

### 2.3. SQL индекси

**Opis rada:**
За потребе система, најкритичнији упити ће бити филтрирање пријава и понуда по статусу и временском периоду, као и претрага по корисницима и одделима. Због тога се креирају индекси:

**На табели `applications`:**
- `idx_applications_status_date` на колонама `application_status`, `applied_at`
- `idx_applications_job_posting` на колони `job_posting_id`
- `idx_applications_candidate` на колони `candidate_id`

**На табели `application_status_history`:**
- `idx_status_history_app_stage` на колонама `application_id`, `stage_id`
- `idx_status_history_entered_at` на колони `entered_at`
- `idx_status_history_exited_at` на колони `exited_at`

**На табели `offers`:**
- `idx_offers_status_date` на колонама `offer_status`, `created_at`
- `idx_offers_application` на колони `application_id`

**На табели `audit_logs`:**
- `idx_audit_time` на колони `time_utc`
- `idx_audit_entity` на колонама `entity_type`, `entity_id`

Ови индекси значајно убрзавају проналажење тренутних статуса, историјских података и генерисање извештаја при великом броју записа.

### 2.4. Извештај који користи PL/SQL

**Svrha:**
Комплексан извештај о перформансама процеса запошљавања који пружа детаљан увид у ефикасност сваке фазе, идентификује уска грла и омогућава доношење одлука на основу података.

**Opis rada:**
Извештај приказује тренутне метрике процеса запошљавања за изабрани временски период и оддел. Кључне функционалности укључују:

**Приказ тренутних статуса:**
- Тренутни статус сваке пријаве у процесу
- Време проведено у свакој фази
- Корисник одговоран за сваку фазу

**Суммарни прегледи по категоријама:**
- Број пријава по статусу и огласу за посао
- Просечно време проведено у свакој фази
- Идентификација фаза са најдужим временом обраде
- Проценат успешних запошљавања по огласима за посао

**Детекција проблема:**
- Пријаве које су "заглављене" у фази дуже од просека
- Фазе са најнижом конверзионом стопом
- Огласи за посао са највишим процентом одбијања понуда

**Кључне метрике (на једном екрану):**
- "колико пријава имамо сада" (тренутни број активних пријава)
- "која фаза је најспорија" (идентификација уских грла)
- "како изгледа структура конверзија" (преглед ефикасности по фазама)
- "који огласи за посао имају најбоље резултате" (поређење перформанси)

**Техничка имплементација:**
Извештај користи следеће PL/SQL компоненте:

**Сложени PL/SQL типови:**
- `recruitment_metrics_type` - за чување основних метрика
- `stage_performance_type` - за метрике по фазама
- `job_posting_summary_type` - за сумарне податке по огласима за посао

**Курсор:**
- `applications_cursor` - за итерацију кроз пријаве у периоду
- `stages_cursor` - за анализу перформанси по фазама

**Сложени SQL упити:**
- Спајање података из најмање 4 табеле: `applications`, `application_status_history`, `offers`, `job_postings`
- Коришћење `GROUP BY` за груписање по статусима, фазама и огласима за посао
- `HAVING` клаузула за филтрирање фаза са ниском конверзионом стопом
- Агрегационе операције `SUM`, `COUNT`, `AVG` за израчунавање метрика
- `WITH` клаузула за креирање привремених резултата за сложене калкулације

**Пример WITH клаузуле:**
```sql
WITH stage_durations AS (
    SELECT 
        ash.stage_id,
        AVG(EXTRACT(EPOCH FROM (ash.exited_at - ash.entered_at))/86400) as avg_days
    FROM application_status_history ash
    WHERE ash.exited_at IS NOT NULL
    GROUP BY ash.stage_id
),
bottleneck_stages AS (
    SELECT stage_id, avg_days
    FROM stage_durations
    WHERE avg_days > (SELECT AVG(avg_days) * 1.5 FROM stage_durations)
)
```

Овај извештај решава проблеме оперативне непрозрачности и управљачке двосмислености, пружајући менаџменту јасан увид у то ко иницира промене, колико су ефективне и где постоје уска грла у процесу запошљавања.

## 3. Закључак

Предложена PL/SQL имплементација ће омогућити:
- Аутоматско праћење свих промена у систему
- Ефикасно израчунавање сложених метрика перформанси
- Брзо генерисање детаљних извештаја
- Идентификацију уских грла и проблема у процесу запошљавања
- Подршку за доношење одлука на основу података

Ова имплементација ће значајно побољшати ефикасност и транспарентност процеса запошљавања у организацији.
