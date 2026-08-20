# Tervehdys

Plugin joka näyttää join/leave-viestit suomeksi tai englanniksi, ja mahdollistaa mukautetun chat-formaatin + LuckPerms support.

## Käyttöönotto

Käännä plugin lähdekoodista:
```bash
./gradlew build
```
Jar löytyy kansiosta `build/libs/`.

Tai lataa suoraan valmis [Release](../../releases).

Laita jar `plugins`-kansioon ja käynnistä servu.

## Kielen vaihto

Defaulttina plugarin kieli on `fi`. Vaihda tarvittaessa `config.yml`:stä `fi` -> `en`.

## Viestien muokkaus

`fi.yml` / `en.yml` -tiedostoista voit muokata pelaajan join- ja leave-viestin. Käytössä on `&`-värikoodit sekä placeholderit `%player%` ja `%lprank%` (vaatii sen LuckPermsin, jos haluaa käyttää %lprank%).

## Mukautettu chat

`config.yml`:stä löytyy `Mukautettu-Chat`-osio, jolla voit korvata servu chat-formaatin (esim. LPC:n tilalle):

```yaml
Mukautettu-Chat:
  Käytössä: false
  Formaatti: "%lprank% %player% &f%message%"
```

Aseta `Käytössä: true` ottaaksesi sen käyttöön. Formaatissa voi käyttää `&`-värikoodeja ja placeholdereita `%lprank%`, `%player%` ja `%message%`. `%lprank%` näyttää pelaajan LuckPerms-rankin (prefixin).

## Komennot

`/tervehdys reload` laittaa muutokset peliin ilman nopeasti ja helposti.