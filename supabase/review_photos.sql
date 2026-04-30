alter table public.reviews
add column if not exists avatar_url text,
add column if not exists photo_urls text[] not null default '{}',
add column if not exists reaction_key text;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'reviews_reaction_key_check'
      and conrelid = 'public.reviews'::regclass
  ) then
    alter table public.reviews
    add constraint reviews_reaction_key_check
    check (
      reaction_key is null or
      reaction_key in ('terrible', 'mid', 'good', 'great', 'incredible')
    );
  end if;
end $$;

-- Public bucket for review photos.
-- The app stores the resulting public URLs in reviews.photo_urls.
insert into storage.buckets (id, name, public)
values ('review-photos', 'review-photos', true)
on conflict (id) do update set public = excluded.public;

drop policy if exists "Anyone can read review photos" on storage.objects;
create policy "Anyone can read review photos"
on storage.objects
for select
using (bucket_id = 'review-photos');

drop policy if exists "Signed in users can upload review photos" on storage.objects;
create policy "Signed in users can upload review photos"
on storage.objects
for insert
with check (bucket_id = 'review-photos' and auth.uid() is not null);
