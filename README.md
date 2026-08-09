# Tervehdys

Plugin joka näyttää join/leave-viestit suomeksi tai englanniksi.

## Käyttöönotto

Käännä plugin lähdekoodista:
```bash
./gradlew build
```
Jar löytyy kansiosta `build/libs/`.

Tai lataa suoraan valmis [Release](../../releases).

Laita jar `plugins`-kansioon ja käynnistä palvelin.

## Kielen vaihto

Defaulttina plugarin kieli on `fi`. Vaihda tarvittaessa `config.yml`:stä `fi` -> `en`.

## Viestien muokkaus

`fi.yml` / `en.yml` -tiedostoista voit muokata pelaajan join- ja leave-viestin.

## Komennot

`/tervehdys reload` laittaa muutokset peliin ilman restarttia.