
create table if not exists public.cafe_crowd_attributes (
  cafe_id text primary key,

  outlet_availability text not null default 'UNKNOWN',
  wifi_name text,
  wifi_speed text not null default 'UNKNOWN',
  wifi_password text,
  wifi_password_known_to_exist boolean not null default false,

  bathroom_availability text not null default 'UNKNOWN',
  bathroom_code text,
  bathroom_code_known_to_exist boolean not null default false,

  seating_availability text not null default 'UNKNOWN',
  seating_space text not null default 'UNKNOWN',
  seating_comfort text not null default 'UNKNOWN',

  crowd_level text not null default 'UNKNOWN',
  noise_level text not null default 'UNKNOWN',
  vibe_tags text[] not null default '{}',

  pet_friendly text not null default 'UNKNOWN',
  cleanliness_rating text not null default 'UNKNOWN',

  seating_photo_urls text[] not null default '{}',
  menu_photo_urls text[] not null default '{}',

  last_updated_epoch_millis bigint not null default ((extract(epoch from now()) * 1000)::bigint),
  updated_by uuid references auth.users(id) on delete set null,
  updated_at timestamptz not null default now()
);

create or replace function public.touch_cafe_crowd_attributes()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  new.updated_at = now();
  if new.last_updated_epoch_millis is null then
    new.last_updated_epoch_millis = (extract(epoch from now()) * 1000)::bigint;
  end if;
  return new;
end;
$$;

drop trigger if exists touch_cafe_crowd_attributes_on_update on public.cafe_crowd_attributes;
create trigger touch_cafe_crowd_attributes_on_update
before update on public.cafe_crowd_attributes
for each row
execute function public.touch_cafe_crowd_attributes();

alter table public.cafe_crowd_attributes enable row level security;

drop policy if exists "Anyone can read cafe crowd attributes" on public.cafe_crowd_attributes;
drop policy if exists "Signed in users can read cafe crowd attributes" on public.cafe_crowd_attributes;
create policy "Signed in users can read cafe crowd attributes"
on public.cafe_crowd_attributes
for select
using (auth.uid() is not null);

drop policy if exists "Signed in users can add cafe crowd attributes" on public.cafe_crowd_attributes;
create policy "Signed in users can add cafe crowd attributes"
on public.cafe_crowd_attributes
for insert
with check (auth.uid() is not null and updated_by = auth.uid());

drop policy if exists "Signed in users can update cafe crowd attributes" on public.cafe_crowd_attributes;
create policy "Signed in users can update cafe crowd attributes"
on public.cafe_crowd_attributes
for update
using (auth.uid() is not null)
with check (auth.uid() is not null and updated_by = auth.uid());

-- Public bucket for community-submitted seating/menu photos.
-- The app stores the resulting public URLs in seating_photo_urls/menu_photo_urls.
insert into storage.buckets (id, name, public)
values ('cafe-crowd-photos', 'cafe-crowd-photos', true)
on conflict (id) do update set public = excluded.public;

drop policy if exists "Anyone can read cafe crowd photos" on storage.objects;
create policy "Anyone can read cafe crowd photos"
on storage.objects
for select
using (bucket_id = 'cafe-crowd-photos');

drop policy if exists "Signed in users can upload cafe crowd photos" on storage.objects;
create policy "Signed in users can upload cafe crowd photos"
on storage.objects
for insert
with check (bucket_id = 'cafe-crowd-photos' and auth.uid() is not null);
