# Silah Modu (Fabric, Minecraft 1.21.11)

Minecraft'a 4 silah ve 4 zırh ekler: **Keskin Nişancı, RPG, Revolver, Bıçak** + **Seviye 1/2 Kask ve Yelek**.

## Kurulum (oynamak için)

1. Minecraft 1.21.11 için [Fabric Loader](https://fabricmc.net/use/installer/) kur.
2. [Fabric API](https://modrinth.com/mod/fabric-api) (1.21.11 sürümü) dosyasını `mods` klasörüne at.
3. Bu modun `.jar` dosyasını da `mods` klasörüne at.

`.jar` dosyasını GitHub'dan almak için: repo > **Actions** > son çalışma > **Artifacts**.

## Kontroller

| Tuş | İşlev |
|---|---|
| Sol tık | Ateş et / bıçakla vur |
| Sağ tık (basılı tut) | Sniper'da scope aç (2.5x) |
| **R** | Şarjör doldur |
| **B** | Silah menüsü (Creative sekmesi "Silahlar" da var) |

Tuşlar Ayarlar > Kontroller > "Silah Modu" altından değiştirilebilir.

## Silahlar (1 kalp = 2 HP)

| Silah | Şarjör / Yedek | Hasar |
|---|---|---|
| Keskin Nişancı | 3 / 12 | Kafa 200 HP, gövde 5 kalp |
| RPG | 3 / 18 | 5x5 alana 5 kalp; tam kafa isabetinde 7,5 kalp |
| Revolver | 8 / 32 | Kafa 200 HP, gövde 4 kalp |
| Bıçak | - | Arkadan 200 HP, önden gövde 3 kalp, önden kafa 6 kalp |

Silahların dayanıklılığı yoktur (sınırsız). RPG roketi giderken arkasından ateş ve duman bırakır.

## Zırhlar (PUBG Mobile tarzı)

Klasik envanterdeki zırh yuvalarından giyilir (kask = kafa yuvası, yelek = göğüs yuvası). B menüsünden, Creative'deki "Silahlar" sekmesinden alınır.

| Eşya | Etkisi |
|---|---|
| Seviye 1 Kask (yeşil) | Demir kask gibi (2 zırh). RPG'nin tam kafa isabeti %25 azalır |
| Seviye 1 Yelek (gri kamuflaj) | Tüm silahların gövde hasarını %15 azaltır |
| Seviye 2 Kask (koyu, siperlikli) | Koruma II büyülü elmas pantolon gibi (6 zırh, 2 sertlik, Koruma II) + 2 altın kalp |
| Seviye 2 Yelek (kahverengi) | Tüm silahların gövde hasarını %30 azaltır |

Gövde hasarı = keskin nişancı/revolver gövde atışı, RPG patlama alanı, bıçağın önden gövde vuruşu. Kafa atışları ve arkadan bıçak (200 HP) yelekten etkilenmez.
Sayılar `WeaponConfig.java` içindeki ZIRHLAR bölümünde. İsimler `lang/*.json` içinde.

Zırh, medkit ve el bombası modelleri + giyilince görünen dokular: `python3 tools/gen_armor.py` (Pillow gerekir).

## Medkit ve El Bombası

- **Medkit** (B menüsü, Zırhlar sütunu): sağ tık basılı tut → ekranın sağ altında kendi skinindeki kol (Steve/Alex otomatik) bandajla sarılır. Süre dolunca can fullenir + 10 kalp geçici sarı kalp (60 sn). Tuşu erken bırakırsan medkit harcanmaz. Süre/kalp/süre ayarı: `WeaponConfig` MEDKIT bölümü. Animasyon `client/MedkitOverlay.java`.
- **El Bombası** (B menüsü, Silahlar sütunu): sağ tık ile atılır, bir şeye çarpınca **TNT ile birebir aynı** patlama (güç 4.0, blok kırar, aynı hasar). TNT görünmez, bomba modeli görünür. Ayar: `WeaponConfig` EL BOMBASI bölümü. Kod: `entity/GrenadeProjectile.java`.

## 3D modeller ve reload animasyonu

Silahlar artik duz sprite degil, kucuk kutulardan olusan 3D modellerdir. Reload (R) sirasinda silah kare kare animasyon oynatir:

| Silah | Animasyon |
|---|---|
| Revolver | Silindir yana acilir, kovanlar duser, hizli doldurucu takilir, silindir kapanir |
| Keskin Nisanci | Surgu kolu kalkar ve geri cekilir, sarjor duser, yenisi takilir, surgu ileri gider |
| RPG | Namlu asagi egilir, eski roket cekilir, yeni roket namluya itilir (mermi bitince roketsiz gorunur) |
| Bicak | Sadece 3D model |

Sniper'da scope acan oyuncunun durbununde, **kendisi hariç herkes** bir isik parlamasi gorur (`WeaponConfig.SNIPER_GLINT*` ile renk/boyut/siklik ayarlanir).

Sniper'da scope acikken (sag tik basili) birinci sahista silah gizlenir, scope katmani zaten ekrani kaplar.

### Nasil calisiyor
Sunucu doldurma sirasinda esyanin `custom_model_data` degerini 1, 2, 3... diye ilerletir.
`assets/silahmod/items/*.json` her degeri bir model dosyasina (`sniper_r1.json`, `sniper_r2.json`...) baglar.
Mixin yoktur, sadece normal model/esya tanimlari kullanilir.

### Modelleri degistirmek
Butun modeller `tools/gen_models.py` icinde kodla tanimlidir. Duzenleyip calistir:

```
python3 tools/gen_models.py      # modelleri yeniden uretir
python3 tools/preview.py         # tools/preview.png onizlemesi (numpy + matplotlib gerekir)
```

- Silahin sekli: dosyadaki `revolver()`, `sniper()`, `rpg()`, `knife()` fonksiyonlari.
- Animasyon kareleri: `REVOLVER_FRAMES`, `SNIPER_FRAMES`, `RPG_FRAMES` listeleri.
- **Kare sayisini degistirirsen** `WeaponConfig.java` icindeki `*_RELOAD_FRAMES` sabitini de ayni sayiya getir.
- Silahin elde duruşu (konum, aci, buyukluk): `WEAPONS` sozlugundeki `fp` (birinci sahis) ve `tp` (ucuncu sahis) satirlari.
- Birinci sahista silahi geriye/sag-alta gomme ayari: `FP_BACK` (gen_models.py) - `shift`, `pitch`, `yaw`. Boyutu etkilemez.
- Animasyon sesleri: `RevolverItem`, `SniperItem`, `RpgItem` icindeki `reloadSounds()` dizileri.
- Renkler: `PALETTE` listesi (tek bir 16x16 palet dokusu uretilir, ayri doku cizmek gerekmez).

## Ayarlar

Tüm hasar, mermi, bekleme ve menzil değerleri tek dosyada:
`src/main/java/com/silahmod/WeaponConfig.java`

Belirtmediğin değerler (atış hızı, doldurma süresi, menzil) benim varsayımımdır; oradan değiştir.

## Derleme (kendi bilgisayarında)

JDK 21 gerekir.

```
./gradlew build
```

Çıktı: `build/libs/silah-modu-1.0.0.jar` (`-sources` olmayan dosya).

## GitHub'a yükleme

```
git init
git add .
git commit -m "Silah modu"
git branch -M main
git remote add origin https://github.com/KULLANICI/REPO.git
git push -u origin main
```

Her push'ta `.github/workflows/build.yml` modu otomatik derler.

## Proje yapısı

- `src/main` : ortak/sunucu kodu (silahlar, hasar, mermi, ağ paketleri)
- `src/client` : sadece istemci (scope, mermi sayacı, B menüsü, zoom)
- `src/main/resources/assets/silahmod` : dokular, modeller, dil dosyaları (TR/EN)
