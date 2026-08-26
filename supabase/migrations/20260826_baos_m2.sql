create extension if not exists pgcrypto;

create table if not exists public.app_users (
  id uuid primary key default gen_random_uuid(),
  truecaller_sub text unique,
  phone text unique,
  given_name text,
  family_name text,
  email text,
  picture_url text,
  role text not null default 'DEALER' check (role in ('SUPER_ADMIN','ADMIN','SALESPERSON','DEALER')),
  phone_verified boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.app_sessions (
  token_hash text primary key,
  user_id uuid not null references public.app_users(id) on delete cascade,
  expires_at timestamptz not null,
  created_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now()
);

create table if not exists public.dealer_profiles (
  user_id uuid primary key references public.app_users(id) on delete cascade,
  business_name text not null default '',
  contact_name text not null default '',
  phone text not null default '',
  email text not null default '',
  gstin text not null default '',
  city text not null default '',
  state text not null default '',
  tier text not null default 'New',
  payment_terms text not null default '15 days',
  credit_limit numeric not null default 100000,
  outstanding numeric not null default 0,
  assigned_salesperson_id text not null default 'sales-ravi',
  photo_url text,
  updated_at timestamptz not null default now()
);

create table if not exists public.products (
  id text primary key,
  sku text unique not null,
  name text not null,
  brand_id text not null,
  category_id text not null,
  description text not null,
  primary_spec text not null,
  pack_size text not null,
  unit text not null default 'pcs',
  moq integer not null default 1,
  stock_state text not null default 'IN_STOCK' check (stock_state in ('IN_STOCK','LIMITED','ON_REQUEST')),
  available_qty integer not null default 0,
  warehouse text not null default 'Pune Main',
  image_url text,
  active boolean not null default true,
  updated_at timestamptz not null default now()
);

create table if not exists public.business_events (
  id uuid primary key default gen_random_uuid(),
  client_event_id text unique,
  user_id uuid references public.app_users(id) on delete set null,
  type text not null,
  entity_type text not null,
  entity_id text not null,
  payload jsonb not null default '{}'::jsonb,
  sync_status text not null default 'received',
  occurred_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

create table if not exists public.approvals (
  id uuid primary key default gen_random_uuid(),
  entity_type text not null,
  entity_id text not null,
  amount numeric,
  reason text not null,
  status text not null default 'PENDING' check (status in ('PENDING','APPROVED','REJECTED','EXPIRED','CANCELLED')),
  requested_by uuid references public.app_users(id) on delete set null,
  decided_by uuid references public.app_users(id) on delete set null,
  requested_at timestamptz not null default now(),
  decided_at timestamptz
);

create table if not exists public.automation_runs (
  id uuid primary key default gen_random_uuid(),
  event_id uuid references public.business_events(id) on delete set null,
  workflow_key text not null,
  status text not null default 'SUCCESS' check (status in ('SUCCESS','WAITING','FAILED','RETRYING')),
  summary text not null,
  details jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create table if not exists public.integration_status (
  key text primary key,
  label text not null,
  mode text not null default 'SIMULATED',
  status text not null default 'READY',
  last_sync_at timestamptz,
  details jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);

create table if not exists public.rfqs (
  id text primary key,
  user_id uuid references public.app_users(id) on delete set null,
  dealer_id text not null,
  reference text not null,
  salesperson_id text,
  status text not null,
  required_by text,
  delivery_city text,
  buyer_reference text,
  note text,
  payload jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.quotes (
  id text primary key,
  user_id uuid references public.app_users(id) on delete set null,
  dealer_id text not null,
  request_id text not null,
  quote_number text not null,
  status text not null,
  approval_status text not null default 'NOT_REQUIRED',
  grand_total numeric not null default 0,
  payload jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.orders (
  id text primary key,
  user_id uuid references public.app_users(id) on delete set null,
  dealer_id text not null,
  request_id text not null,
  quote_id text not null,
  order_number text not null,
  status text not null,
  payment_status text not null,
  total numeric not null default 0,
  dispatch_reference text,
  payload jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_sessions_user on public.app_sessions(user_id);
create index if not exists idx_events_entity on public.business_events(entity_type, entity_id);
create index if not exists idx_events_type on public.business_events(type, created_at desc);
create index if not exists idx_runs_created on public.automation_runs(created_at desc);
create index if not exists idx_rfqs_user on public.rfqs(user_id, updated_at desc);
create index if not exists idx_quotes_user on public.quotes(user_id, updated_at desc);
create index if not exists idx_orders_user on public.orders(user_id, updated_at desc);

alter table public.app_users enable row level security;
alter table public.app_sessions enable row level security;
alter table public.dealer_profiles enable row level security;
alter table public.products enable row level security;
alter table public.business_events enable row level security;
alter table public.approvals enable row level security;
alter table public.automation_runs enable row level security;
alter table public.integration_status enable row level security;
alter table public.rfqs enable row level security;
alter table public.quotes enable row level security;
alter table public.orders enable row level security;

-- No broad anon/authenticated policies are intentionally created.
-- The Edge Functions use the Supabase service-role key server-side and enforce app-session checks.

insert into public.integration_status(key,label,mode,status,details) values
 ('supabase','Supabase Backend','LIVE','READY','{}'),
 ('truecaller','Truecaller OAuth','LIVE','READY','{}'),
 ('payments','BAOS Demo Payment','SIMULATED','READY','{}'),
 ('tally','TallyPrime','SIMULATED','READY','{}'),
 ('whatsapp','WhatsApp Business','SIMULATED','READY','{}'),
 ('n8n','n8n Orchestrator','SIMULATED','READY','{}')
on conflict (key) do update set label=excluded.label, mode=excluded.mode, status=excluded.status, updated_at=now();

insert into public.products(id,sku,name,brand_id,category_id,description,primary_spec,pack_size,unit,moq,stock_state,available_qty,warehouse,image_url,active) values
 ('prd-bear-6204','BRG-6204-2RS','6204 2RS Deep Groove Ball Bearing','brand-skf','cat-bearings','Sealed deep-groove bearing for motors, pumps and general industrial service.','20 × 47 × 14 mm','10 pcs','pcs',10,'IN_STOCK',148,'Pune Main','https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=900&q=82',true),
 ('prd-bear-6205','BRG-6205-ZZ','6205 ZZ Deep Groove Ball Bearing','brand-nsk','cat-bearings','Shielded bearing for high-speed industrial equipment and maintenance use.','25 × 52 × 15 mm','10 pcs','pcs',10,'IN_STOCK',96,'Bhosari','https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=900&q=82',true),
 ('prd-belt-a42','BLT-A42','A42 Classical V-Belt','brand-optibelt','cat-belts','Wrapped V-belt for industrial drives, fans and workshop machinery.','A section · 42 in','5 pcs','pcs',5,'IN_STOCK',82,'Pune Main','https://images.unsplash.com/photo-1504917595217-d4dc5ebe6122?auto=format&fit=crop&w=900&q=82',true),
 ('prd-belt-b56','BLT-B56','B56 Industrial V-Belt','brand-fenner','cat-belts','Heavy-duty classical V-belt for industrial power transmission.','B section · 56 in','5 pcs','pcs',5,'LIMITED',22,'Chakan','https://images.unsplash.com/photo-1565043666747-69f6646db940?auto=format&fit=crop&w=900&q=82',true),
 ('prd-motor-1hp','MTR-1HP-4P','1 HP 4 Pole Three Phase Motor','brand-abb','cat-motors','Industrial induction motor for pumps, conveyors and OEM equipment.','1 HP · 4 pole · 415 V','1 pc','pcs',1,'IN_STOCK',34,'Pune Main','https://images.unsplash.com/photo-1530124566582-a618bc2615dc?auto=format&fit=crop&w=900&q=82',true),
 ('prd-motor-3hp','MTR-3HP-4P','3 HP 4 Pole Three Phase Motor','brand-cg','cat-motors','TEFC industrial motor for continuous-duty machinery and process equipment.','3 HP · 4 pole · 415 V','1 pc','pcs',1,'LIMITED',12,'Chakan','https://images.unsplash.com/photo-1586864387967-d02ef85d93e8?auto=format&fit=crop&w=900&q=82',true),
 ('prd-contactor-18a','ELE-C18','18A Three Pole Contactor','brand-schneider','cat-electrical','Industrial contactor for motor control panels and automation systems.','18 A · 3 pole · 230 V coil','4 pcs','pcs',4,'IN_STOCK',64,'Bhosari','https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?auto=format&fit=crop&w=900&q=82',true),
 ('prd-mccb-63a','ELE-MCCB63','63A 4P MCCB','brand-lt','cat-electrical','Moulded case circuit breaker for industrial distribution and machine panels.','63 A · 4 pole','1 pc','pcs',1,'IN_STOCK',29,'Pune Main','https://images.unsplash.com/photo-1513828583688-c52646db42da?auto=format&fit=crop&w=900&q=82',true),
 ('prd-bolt-m10','FST-M10X50','M10 × 50 Hex Bolt Grade 8.8','brand-apex','cat-fasteners','High-tensile zinc-plated hex bolt for industrial assemblies.','M10 × 50 · grade 8.8','100 pcs','pcs',100,'IN_STOCK',1200,'Pune Main','https://images.unsplash.com/photo-1565043666747-69f6646db940?auto=format&fit=crop&w=900&q=82',true),
 ('prd-nut-m10','FST-NUT-M10','M10 Hex Nut Grade 8','brand-apex','cat-fasteners','High-tensile metric hex nut for structural and machinery applications.','M10 · grade 8','100 pcs','pcs',100,'IN_STOCK',1800,'Bhosari','https://images.unsplash.com/photo-1504917595217-d4dc5ebe6122?auto=format&fit=crop&w=900&q=82',true),
 ('prd-grinder','TLS-AG100','100 mm Angle Grinder','brand-bosch','cat-tools','Compact professional angle grinder for fabrication and maintenance teams.','100 mm · 850 W','1 pc','pcs',1,'LIMITED',18,'Pune Main','https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=900&q=82',true),
 ('prd-drill','TLS-ID13','13 mm Impact Drill','brand-bosch','cat-tools','Professional impact drill for maintenance, installation and workshop use.','13 mm · 650 W','1 pc','pcs',1,'ON_REQUEST',7,'Chakan','https://images.unsplash.com/photo-1572981779307-38b8cabb2407?auto=format&fit=crop&w=900&q=82',true)
on conflict (id) do update set
  sku=excluded.sku,name=excluded.name,brand_id=excluded.brand_id,category_id=excluded.category_id,description=excluded.description,
  primary_spec=excluded.primary_spec,pack_size=excluded.pack_size,unit=excluded.unit,moq=excluded.moq,stock_state=excluded.stock_state,
  available_qty=excluded.available_qty,warehouse=excluded.warehouse,image_url=excluded.image_url,active=excluded.active,updated_at=now();
